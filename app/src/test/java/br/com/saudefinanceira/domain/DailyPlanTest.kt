package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.IncomeEntity
import br.com.saudefinanceira.data.QuickExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DailyPlanTest {
    private fun expense(y: Int, m: Int, d: Int, cents: Long) =
        QuickExpenseEntity(amountCents = cents, note = "x", year = y, month = m, day = d)

    @Test
    fun cycleWithoutPayDayIsTheCalendarMonth() {
        val c = PayCycle.of(LocalDate.of(2026, 11, 10), null)
        assertEquals(LocalDate.of(2026, 11, 1), c.start)
        assertEquals(LocalDate.of(2026, 11, 30), c.end)
        assertEquals(21, c.daysLeft(LocalDate.of(2026, 11, 10)))
    }

    @Test
    fun cycleStartsOnLastPayDay() {
        val c = PayCycle.of(LocalDate.of(2026, 11, 10), 5)
        assertEquals(LocalDate.of(2026, 11, 5), c.start)
        assertEquals(LocalDate.of(2026, 12, 4), c.end)
        assertEquals(25, c.daysLeft(LocalDate.of(2026, 11, 10)))
    }

    @Test
    fun cycleBeforePayDayStartsInPreviousMonth() {
        val c = PayCycle.of(LocalDate.of(2026, 11, 3), 5)
        assertEquals(LocalDate.of(2026, 10, 5), c.start)
        assertEquals(LocalDate.of(2026, 11, 4), c.end)
        assertEquals(2, c.daysLeft(LocalDate.of(2026, 11, 3)))
    }

    @Test
    fun payDay31IsClampedToShortMonths() {
        val c = PayCycle.of(LocalDate.of(2026, 2, 28), 31)
        assertEquals(LocalDate.of(2026, 2, 28), c.start)
        assertEquals(LocalDate.of(2026, 3, 30), c.end)
    }

    @Test
    fun mainPayDayPrefersSalaryAndIgnoresBenefits() {
        val vr = IncomeEntity(1, "VR", 50_000, 10)
        val other = IncomeEntity(2, "Outra renda", 10_000, 20)
        val salary = IncomeEntity(3, "Salário", 150_000, 5)
        assertEquals(5, PayCycle.mainPayDay(listOf(vr, other, salary)))
        assertEquals(20, PayCycle.mainPayDay(listOf(vr, other)))
        assertNull(PayCycle.mainPayDay(listOf(vr, IncomeEntity(4, "Salário", 100_000))))
    }

    @Test
    fun dailyBudgetSplitsWhatIsLeftByRemainingDays() {
        val s = Snapshot(incomeCents = 150_000, fixedCents = 80_000, variableCents = 20_000, installmentsCents = 0)
        val b = DailyBudgetCalc.compute(s, 5_000, emptyList(), LocalDate.of(2026, 11, 10), null)
        assertEquals(45_000, b.freeCents)
        assertEquals(21, b.daysLeft)
        assertEquals(2_142, b.todayBudgetCents)
        assertEquals(2_142, b.leftTodayCents)
    }

    @Test
    fun dailyBudgetCountsOnlyExpensesInsideTheCycle() {
        val s = Snapshot(incomeCents = 150_000, fixedCents = 80_000, variableCents = 20_000, installmentsCents = 0)
        val expenses = listOf(
            expense(2026, 11, 10, 1_000),
            expense(2026, 11, 2, 3_000),
            expense(2026, 10, 31, 9_999),
        )
        val b = DailyBudgetCalc.compute(s, 5_000, expenses, LocalDate.of(2026, 11, 10), null)
        assertEquals(4_000, b.spentCycleCents)
        assertEquals(1_000, b.spentTodayCents)
        assertEquals(41_000, b.remainingCents)
        assertEquals(2_000, b.todayBudgetCents)
        assertEquals(1_000, b.leftTodayCents)
    }

    @Test
    fun dailyBudgetIsZeroWhenBillsExceedIncome() {
        val s = Snapshot(incomeCents = 100_000, fixedCents = 120_000, variableCents = 0, installmentsCents = 0)
        val b = DailyBudgetCalc.compute(s, 0, emptyList(), LocalDate.of(2026, 11, 10), null)
        assertEquals(-20_000, b.freeCents)
        assertEquals(0, b.todayBudgetCents)
    }

    @Test
    fun spendingMoreThanTodayLimitGoesNegative() {
        val s = Snapshot(incomeCents = 150_000, fixedCents = 80_000, variableCents = 20_000, installmentsCents = 0)
        val b = DailyBudgetCalc.compute(s, 5_000, listOf(expense(2026, 11, 10, 5_000)), LocalDate.of(2026, 11, 10), null)
        assertEquals(true, b.leftTodayCents < 0)
    }
}
