package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class DueBill(val bill: BillEntity, val dueDate: LocalDate, val daysUntil: Long)

object ReminderLogic {
    /** Próxima data de vencimento a partir de hoje (dia 31 em mês curto vira o último dia do mês). */
    fun nextDueDate(dueDay: Int, today: LocalDate): LocalDate {
        val thisMonth = YearMonth.from(today)
        val candidate = thisMonth.atDay(minOf(dueDay, thisMonth.lengthOfMonth()))
        if (!candidate.isBefore(today)) return candidate
        val next = thisMonth.plusMonths(1)
        return next.atDay(minOf(dueDay, next.lengthOfMonth()))
    }

    /** Contas com vencimento hoje ou nos próximos [leadDays] dias, das mais próximas às mais distantes. */
    fun dueSoon(bills: List<BillEntity>, today: LocalDate, leadDays: Int): List<DueBill> =
        bills.mapNotNull { bill ->
            val day = bill.dueDay ?: return@mapNotNull null
            val due = nextDueDate(day, today)
            val days = ChronoUnit.DAYS.between(today, due)
            if (days >= 0 && days <= leadDays) DueBill(bill, due, days) else null
        }.sortedBy { it.daysUntil }
}
