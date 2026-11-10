package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.IncomeEntity
import br.com.saudefinanceira.data.QuickExpenseEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Período entre dois recebimentos. Sem dia de recebimento informado, usa o mês do calendário.
 * [end] é o último dia do ciclo (inclusive).
 */
data class PayCycle(val start: LocalDate, val end: LocalDate) {
    /** Dias que faltam até o fim do ciclo, contando hoje. Nunca menos que 1. */
    fun daysLeft(today: LocalDate): Int =
        (ChronoUnit.DAYS.between(today, end) + 1).toInt().coerceAtLeast(1)

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    companion object {
        fun of(today: LocalDate, payDay: Int?): PayCycle {
            if (payDay == null) {
                val ym = YearMonth.from(today)
                return PayCycle(ym.atDay(1), ym.atEndOfMonth())
            }
            val wanted: Int = payDay
            fun dayIn(ym: YearMonth): LocalDate = ym.atDay(wanted.coerceIn(1, ym.lengthOfMonth()))
            val thisMonth = YearMonth.from(today)
            val start = if (!today.isBefore(dayIn(thisMonth))) dayIn(thisMonth) else dayIn(thisMonth.minusMonths(1))
            val nextStart = dayIn(YearMonth.from(start).plusMonths(1))
            return PayCycle(start, nextStart.minusDays(1))
        }

        /** Dia de recebimento do salário (ou da primeira renda em dinheiro com dia informado). */
        fun mainPayDay(incomes: List<IncomeEntity>): Int? {
            val cash = incomes.filter { it.description.uppercase() !in setOf("VR", "VM") && it.payDay != null }
            return (cash.firstOrNull { it.description == "Salário" } ?: cash.firstOrNull())?.payDay
        }
    }
}

data class DailyBudget(
    val cycle: PayCycle,
    val daysLeft: Int,
    /** Renda menos compromissos do mês e reserva mínima. Negativo = as contas já passam da renda. */
    val freeCents: Long,
    val spentCycleCents: Long,
    val spentTodayCents: Long,
    /** O que ainda sobra no ciclo (livre menos o que já foi gasto no dia a dia). */
    val remainingCents: Long,
    /** Limite de hoje, calculado no começo do dia. */
    val todayBudgetCents: Long,
    /** Quanto ainda pode gastar hoje. Negativo = passou do limite. */
    val leftTodayCents: Long,
)

object DailyBudgetCalc {
    fun compute(
        snapshot: Snapshot,
        reserveCents: Long,
        expenses: List<QuickExpenseEntity>,
        today: LocalDate,
        payDay: Int?,
    ): DailyBudget {
        val cycle = PayCycle.of(today, payDay)
        val daysLeft = cycle.daysLeft(today)
        val dated = expenses.mapNotNull { e ->
            runCatching { LocalDate.of(e.year, e.month, e.day) }.getOrNull()?.let { it to e.amountCents }
        }
        val spentCycle = dated.filter { cycle.contains(it.first) }.sumOf { it.second }
        val spentToday = dated.filter { it.first == today }.sumOf { it.second }
        val free = snapshot.balanceCents - reserveCents.coerceAtLeast(0L)
        val remaining = free - spentCycle
        val startOfDay = remaining + spentToday
        val todayBudget = if (startOfDay <= 0L) 0L else startOfDay / daysLeft
        return DailyBudget(
            cycle = cycle,
            daysLeft = daysLeft,
            freeCents = free,
            spentCycleCents = spentCycle,
            spentTodayCents = spentToday,
            remainingCents = remaining,
            todayBudgetCents = todayBudget,
            leftTodayCents = todayBudget - spentToday,
        )
    }
}
