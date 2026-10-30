package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.InstallmentEntity
import java.time.YearMonth

/** [months]: total de parcelas do cartão no mês atual e nos meses seguintes. */
data class CardInvoice(val name: String, val months: List<Long>, val lastMonth: YearMonth)

object CardInvoices {
    const val NO_CARD = "Sem cartão informado"

    /** Parcelas agrupadas por cartão (ignora maiúsculas/espaços). Cartões já quitados não aparecem. */
    fun compute(items: List<InstallmentEntity>, now: YearMonth, monthsAhead: Int = 3): List<CardInvoice> =
        items
            .groupBy { (it.card ?: "").trim().lowercase() }
            .values
            .mapNotNull { list ->
                val last = list.maxOf { YearMonth.of(it.firstYear, it.firstMonth).plusMonths((it.installments - 1).toLong()) }
                if (last.isBefore(now)) return@mapNotNull null
                val name = list.first().card?.trim().takeUnless { it.isNullOrEmpty() } ?: NO_CARD
                val months = (0..monthsAhead).map { InstallmentCalc.monthlyTotal(list, now.plusMonths(it.toLong())) }
                CardInvoice(name, months, last)
            }
            .sortedByDescending { it.months.first() }
}
