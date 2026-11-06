package br.com.saudefinanceira.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.saudefinanceira.data.AppDatabase
import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillEntryEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.DebtEntity
import br.com.saudefinanceira.data.GoalEntity
import br.com.saudefinanceira.data.IncomeEntity
import br.com.saudefinanceira.data.MonthSnapshotEntity
import br.com.saudefinanceira.data.InstallmentEntity
import br.com.saudefinanceira.domain.BillStatsCalc
import br.com.saudefinanceira.domain.InstallmentCalc
import br.com.saudefinanceira.domain.Snapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class FinanceViewModel(db: AppDatabase) : ViewModel() {
    private val dao = db.dao()

    val snapshot: StateFlow<Snapshot> = combine(
        dao.observeIncome(),
        dao.observeBills(),
        dao.observeInstallments(),
        dao.observeDebts(),
    ) { income, bills, installments, debts ->
        val now = YearMonth.now()
        val benefits = income.filter { it.description.uppercase() in setOf("VR", "VM") }
        val benefitPurchases = installments.filter { it.paymentMethod.uppercase() in setOf("VR", "VM") }
        Snapshot(
            incomeCents = income.filterNot { it.description.uppercase() in setOf("VR", "VM") }.sumOf { it.amountCents },
            fixedCents = bills.filter { it.type == BillType.FIXED }.sumOf { it.amountCents },
            variableCents = bills.filter { it.type == BillType.VARIABLE }.sumOf { it.amountCents },
            installmentsCents = InstallmentCalc.monthlyTotal(
                installments.filterNot { it.paymentMethod.uppercase() in setOf("VR", "VM") }, now,
            ),
            benefitIncomeCents = benefits.sumOf { it.amountCents },
            benefitSpentCents = InstallmentCalc.monthlyTotal(benefitPurchases, now),
            debtPaymentsCents = debts.sumOf { if (it.balanceCents > 0L) it.plannedPaymentCents else 0L },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Snapshot(0, 0, 0, 0))

    val incomes: StateFlow<List<IncomeEntity>> =
        dao.observeIncome().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val debts: StateFlow<List<DebtEntity>> =
        dao.observeDebts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bills: StateFlow<List<BillEntity>> =
        dao.observeBills().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val installments: StateFlow<List<InstallmentEntity>> =
        dao.observeInstallments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val entries: StateFlow<List<BillEntryEntity>> =
        dao.observeEntries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<List<GoalEntity>> =
        dao.observeGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Contas variáveis que subiram 15% ou mais em relação à média anterior (nome, %). */
    val risingBills: StateFlow<List<Pair<String, Int>>> = combine(
        dao.observeBills(),
        dao.observeEntries(),
    ) { bills, entries ->
        val now = YearMonth.now()
        val byBill = entries.groupBy { it.billId }
        bills.filter { it.type == BillType.VARIABLE }.mapNotNull { b ->
            val trend = BillStatsCalc.compute(byBill[b.id].orEmpty(), now)?.trendPercent
            if (trend != null && trend >= 15) b.name to trend else null
        }.sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<MonthSnapshotEntity>> =
        dao.observeSnapshots().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Contas variáveis existentes antes do histórico começam a registrar o valor atual
        // no mês presente. O app não inventa valores para meses anteriores.
        viewModelScope.launch {
            combine(dao.observeBills(), dao.observeEntries()) { bills, entries -> bills to entries }
                .collect { (bills, entries) ->
                    val now = YearMonth.now()
                    val currentEntries = entries.asSequence()
                        .filter { it.year == now.year && it.month == now.monthValue }
                        .map { it.billId }
                        .toSet()
                    bills.asSequence()
                        .filter { it.type == BillType.VARIABLE && it.id !in currentEntries }
                        .forEach { bill ->
                            dao.upsertEntry(
                                BillEntryEntity(
                                    billId = bill.id,
                                    year = now.year,
                                    month = now.monthValue,
                                    amountCents = bill.amountCents,
                                )
                            )
                        }
                }
        }

        // Registra o resumo do mês atual sempre que os números mudam (o histórico começa a partir desta versão).
        viewModelScope.launch {
            snapshot.collect { s ->
                if (s.incomeCents > 0L || s.spentCents > 0L) {
                    val now = YearMonth.now()
                    dao.upsertSnapshot(
                        MonthSnapshotEntity(
                            year = now.year,
                            month = now.monthValue,
                            incomeCents = s.incomeCents,
                            fixedCents = s.fixedCents,
                            variableCents = s.variableCents,
                            installmentsCents = s.installmentsCents,
                            debtPaymentsCents = s.debtPaymentsCents,
                        )
                    )
                }
            }
        }
    }

    fun saveIncome(income: IncomeEntity) {
        viewModelScope.launch { dao.upsertIncome(income) }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch { dao.deleteIncome(income) }
    }

    fun saveDebt(debt: DebtEntity) {
        viewModelScope.launch { if (debt.id == 0L) dao.insertDebt(debt) else dao.updateDebt(debt) }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { dao.deleteDebt(debt) }
    }

    fun saveBill(bill: BillEntity) {
        viewModelScope.launch {
            val id = if (bill.id == 0L) dao.insertBill(bill) else { dao.updateBill(bill); bill.id }
            if (bill.type == BillType.VARIABLE) {
                val now = YearMonth.now()
                dao.upsertEntry(BillEntryEntity(billId = id, year = now.year, month = now.monthValue, amountCents = bill.amountCents))
            }
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            dao.deleteEntriesOf(bill.id)
            dao.deleteBill(bill)
        }
    }

    fun saveBillEntry(entry: BillEntryEntity) {
        viewModelScope.launch {
            dao.upsertEntry(entry)
            val now = YearMonth.now()
            if (entry.year == now.year && entry.month == now.monthValue) {
                dao.getBill(entry.billId)?.let { dao.updateBill(it.copy(amountCents = entry.amountCents)) }
            }
        }
    }

    fun deleteBillEntry(entry: BillEntryEntity) {
        viewModelScope.launch { dao.deleteEntry(entry.id) }
    }

    fun saveInstallment(item: InstallmentEntity) {
        viewModelScope.launch { if (item.id == 0L) dao.insertInstallment(item) else dao.updateInstallment(item) }
    }

    fun deleteInstallment(item: InstallmentEntity) {
        viewModelScope.launch { dao.deleteInstallment(item) }
    }

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch { if (goal.id == 0L) dao.insertGoal(goal) else dao.updateGoal(goal) }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { dao.deleteGoal(goal) }
    }

    /** [deltaCents] positivo guarda, negativo retira. O valor guardado nunca fica negativo. */
    fun addToGoal(goal: GoalEntity, deltaCents: Long) {
        viewModelScope.launch {
            dao.updateGoal(goal.copy(savedCents = (goal.savedCents + deltaCents).coerceAtLeast(0L)))
        }
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FinanceViewModel(db) as T
    }
}
