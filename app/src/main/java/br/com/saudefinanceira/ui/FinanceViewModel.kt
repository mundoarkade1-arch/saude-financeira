package br.com.saudefinanceira.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.FinanceDao
import br.com.saudefinanceira.data.IncomeEntity
import br.com.saudefinanceira.data.InstallmentEntity
import br.com.saudefinanceira.domain.InstallmentCalc
import br.com.saudefinanceira.domain.Snapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class FinanceViewModel(private val dao: FinanceDao) : ViewModel() {

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

    fun saveSalary(cents: Long) {
        viewModelScope.launch { dao.upsertIncome(IncomeEntity(id = 1, description = "Salário", amountCents = cents)) }
    }

    fun saveBill(bill: BillEntity) {
        viewModelScope.launch { if (bill.id == 0L) dao.insertBill(bill) else dao.updateBill(bill) }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch { dao.deleteBill(bill) }
    }

    fun saveInstallment(item: InstallmentEntity) {
        viewModelScope.launch { dao.insertInstallment(item) }
    }

    fun deleteInstallment(item: InstallmentEntity) {
        viewModelScope.launch { dao.deleteInstallment(item) }
    }

    class Factory(private val dao: FinanceDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FinanceViewModel(dao) as T
    }
}
