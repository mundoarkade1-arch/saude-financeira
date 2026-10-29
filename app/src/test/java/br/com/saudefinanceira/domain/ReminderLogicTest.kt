package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReminderLogicTest {
    private fun bill(id: Long, name: String, day: Int?) =
        BillEntity(id = id, name = name, type = BillType.FIXED, amountCents = 10_000, dueDay = day)

    @Test
    fun dueTodayCountsAsToday() {
        assertEquals(LocalDate.of(2026, 10, 29), ReminderLogic.nextDueDate(29, LocalDate.of(2026, 10, 29)))
    }

    @Test
    fun passedDayRollsToNextMonth() {
        assertEquals(LocalDate.of(2026, 11, 5), ReminderLogic.nextDueDate(5, LocalDate.of(2026, 10, 29)))
    }

    @Test
    fun day31InShortMonthBecomesLastDay() {
        assertEquals(LocalDate.of(2026, 2, 28), ReminderLogic.nextDueDate(31, LocalDate.of(2026, 2, 10)))
        assertEquals(LocalDate.of(2026, 11, 30), ReminderLogic.nextDueDate(31, LocalDate.of(2026, 11, 15)))
    }

    @Test
    fun yearEndRollsToJanuary() {
        assertEquals(LocalDate.of(2027, 1, 5), ReminderLogic.nextDueDate(5, LocalDate.of(2026, 12, 20)))
    }

    @Test
    fun dueSoonFiltersAndSorts() {
        val today = LocalDate.of(2026, 10, 29)
        val bills = listOf(
            bill(1, "A", 30),   // amanhã
            bill(2, "B", 29),   // hoje
            bill(3, "C", 5),    // 7 dias
            bill(4, "D", null), // sem vencimento
        )
        val result = ReminderLogic.dueSoon(bills, today, leadDays = 1)
        assertEquals(listOf("B", "A"), result.map { it.bill.name })
        assertEquals(listOf(0L, 1L), result.map { it.daysUntil })
    }

    @Test
    fun leadZeroOnlyToday() {
        val today = LocalDate.of(2026, 10, 29)
        val result = ReminderLogic.dueSoon(listOf(bill(1, "A", 30), bill(2, "B", 29)), today, leadDays = 0)
        assertEquals(listOf("B"), result.map { it.bill.name })
    }
}
