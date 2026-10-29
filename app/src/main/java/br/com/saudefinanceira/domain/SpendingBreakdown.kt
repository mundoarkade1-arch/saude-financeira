package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.InstallmentEntity
import java.time.YearMonth

enum class SpendKind { FIXED, VARIABLE, INSTALLMENT }

data class SpendItem(val name: String, val cents: Long, val kind: SpendKind)

object SpendingBreakdown {
    /** Gastos previstos do mês [now], do maior para o menor (parcelas só entram se vencem neste mês). */
    fun items(bills: List<BillEntity>, installments: List<InstallmentEntity>, now: YearMonth): List<SpendItem> {
        val fromBills = bills.map {
            SpendItem(it.name, it.amountCents, if (it.type == BillType.FIXED) SpendKind.FIXED else SpendKind.VARIABLE)
        }
        val fromInstallments = installments.map {
            SpendItem(it.description, InstallmentCalc.status(it, now).monthlyCents, SpendKind.INSTALLMENT)
        }
        return (fromBills + fromInstallments).filter { it.cents > 0L }.sortedByDescending { it.cents }
    }
}
