package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.MonthSnapshotEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class HistoryCalcTest {
    private fun snap(year: Int, month: Int, income: Long, fixed: Long = 0, variable: Long = 0, inst: Long = 0) =
        MonthSnapshotEntity(
            year = year, month = month, incomeCents = income,
            fixedCents = fixed, variableCents = variable, installmentsCents = inst,
        )

    @Test
    fun balanceIsIncomeMinusAllSpending() {
        assertEquals(1_500L, HistoryCalc.balanceOf(snap(2026, 10, 10_000, 5_000, 2_500, 1_000)))
    }

    @Test
    fun recentIsChronologicalAndLimited() {
        val entries = (1..8).map { snap(2026, it, 10_000L * it) }
        val recent = HistoryCalc.recent(entries, limit = 6)
        assertEquals(6, recent.size)
        assertEquals(YearMonth.of(2026, 3), recent.first().month)
        assertEquals(YearMonth.of(2026, 8), recent.last().month)
    }

    @Test
    fun crossesYearBoundaryInOrder() {
        val recent = HistoryCalc.recent(listOf(snap(2027, 1, 100), snap(2026, 12, 100), snap(2026, 11, 100)))
        assertEquals(
            listOf(YearMonth.of(2026, 11), YearMonth.of(2026, 12), YearMonth.of(2027, 1)),
            recent.map { it.month },
        )
    }

    @Test
    fun deltaComparesLastTwoMonths() {
        val recent = HistoryCalc.recent(listOf(snap(2026, 9, 10_000, 4_000), snap(2026, 10, 10_000, 3_000)))
        assertEquals(1_000L, HistoryCalc.deltaVsPrevious(recent))
    }

    @Test
    fun deltaIsNullWithSingleMonth() {
        assertNull(HistoryCalc.deltaVsPrevious(HistoryCalc.recent(listOf(snap(2026, 10, 10_000)))))
    }
}
