package br.com.saudefinanceira.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.FinanceDao
import br.com.saudefinanceira.data.IncomeEntity
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

    fun saveSalary(cents: Long) {
        viewModelScope.launch { dao.upsertIncome(IncomeEntity(id = 1, description = "Salário", amountCents = cents)) }
    }

    class Factory(private val dao: FinanceDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FinanceViewModel(dao) as T
    }
}
