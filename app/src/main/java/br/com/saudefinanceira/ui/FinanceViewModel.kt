package br.com.saudefinanceira.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.saudefinanceira.data.AppDatabase
import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillEntryEntity
import br.com.saudefinanceira.data.BillType
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
    ) { income, bills, installments ->
        Snapshot(
            incomeCents = income.sumOf { it.amountCents },
            fixedCents = bills.filter { it.type == BillType.FIXED }.sumOf { it.amountCents },
            variableCents = bills.filter { it.type == BillType.VARIABLE }.sumOf { it.amountCents },
            installmentsCents = InstallmentCalc.monthlyTotal(installments, YearMonth.now()),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Snapshot(0, 0, 0, 0))

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
                        )
                    )
                }
            }
        }
    }

    fun saveSalary(cents: Long) {
        viewModelScope.launch { dao.upsertIncome(IncomeEntity(id = 1, description = "Salário", amountCents = cents)) }
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
