package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class UpcomingBill(val bill: BillEntity, val daysUntil: Int)

object UpcomingBills {
    /** Dias até o próximo vencimento do dia [dueDay] (0 = hoje). Meses curtos usam o último dia. */
    fun daysUntil(dueDay: Int, today: LocalDate): Int {
        val thisMonth = YearMonth.from(today)
        val thisDue = thisMonth.atDay(dueDay.coerceIn(1, thisMonth.lengthOfMonth()))
        if (!thisDue.isBefore(today)) return ChronoUnit.DAYS.between(today, thisDue).toInt()
        val nextMonth = thisMonth.plusMonths(1)
        val nextDue = nextMonth.atDay(dueDay.coerceIn(1, nextMonth.lengthOfMonth()))
        return ChronoUnit.DAYS.between(today, nextDue).toInt()
    }

    /** Contas com vencimento informado que vencem em até [withinDays] dias, das mais próximas para as mais distantes. */
    fun next(bills: List<BillEntity>, today: LocalDate, withinDays: Int = 7): List<UpcomingBill> =
        bills.mapNotNull { bill ->
            val day = bill.dueDay ?: return@mapNotNull null
            UpcomingBill(bill, daysUntil(day, today))
        }
            .filter { it.daysUntil <= withinDays }
            .sortedWith(compareBy({ it.daysUntil }, { it.bill.priority }, { it.bill.name.lowercase() }))
}
