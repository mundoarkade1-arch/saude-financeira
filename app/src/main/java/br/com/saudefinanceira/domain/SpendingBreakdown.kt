package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.DebtEntity
import br.com.saudefinanceira.data.InstallmentEntity
import java.time.YearMonth

enum class SpendKind { FIXED, VARIABLE, INSTALLMENT, BENEFIT, DEBT }

data class SpendItem(val name: String, val cents: Long, val kind: SpendKind)

object SpendingBreakdown {
    /** Gastos previstos do mês [now], do maior para o menor (parcelas só entram se vencem neste mês). */
    fun items(bills: List<BillEntity>, installments: List<InstallmentEntity>, now: YearMonth, debts: List<DebtEntity> = emptyList()): List<SpendItem> {
        val fromBills = bills.map {
            SpendItem(it.name, it.amountCents, if (it.type == BillType.FIXED) SpendKind.FIXED else SpendKind.VARIABLE)
        }
        val fromInstallments = installments.map {
            val kind = if (it.paymentMethod.uppercase() in setOf("VR", "VM")) SpendKind.BENEFIT else SpendKind.INSTALLMENT
            SpendItem(it.description, InstallmentCalc.status(it, now).monthlyCents, kind)
        }
        val fromDebts = debts.filter { it.balanceCents > 0L && it.plannedPaymentCents > 0L }
            .map { SpendItem("Acordo • ${it.creditor}", it.plannedPaymentCents, SpendKind.DEBT) }
        return (fromBills + fromInstallments + fromDebts).filter { it.cents > 0L }.sortedByDescending { it.cents }
    }
}
