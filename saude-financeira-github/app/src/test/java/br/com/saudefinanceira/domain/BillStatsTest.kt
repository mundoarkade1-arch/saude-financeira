package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class BillStatsTest {
    private fun e(year: Int, month: Int, cents: Long) =
        BillEntryEntity(billId = 1, year = year, month = month, amountCents = cents)

    @Test
    fun noEntriesMeansNoStats() {
        assertNull(BillStatsCalc.compute(emptyList(), YearMonth.of(2026, 10)))
    }

    @Test
    fun computesAverageAndTrend() {
        val entries = listOf(e(2026, 8, 10_000), e(2026, 9, 10_000), e(2026, 10, 15_000))
        val s = BillStatsCalc.compute(entries, YearMonth.of(2026, 10))!!
        assertEquals(3, s.months)
        assertEquals(11_666L, s.averageCents)
        assertEquals(50, s.trendPercent)
        assertNull(s.staleSince)
    }

    @Test
    fun singleEntryHasNoTrend() {
        val s = BillStatsCalc.compute(listOf(e(2026, 10, 9_000)), YearMonth.of(2026, 10))!!
        assertNull(s.trendPercent)
        assertEquals(9_000L, s.averageCents)
    }

    @Test
    fun marksStaleWhenLatestIsOlderThanCurrentMonth() {
        val s = BillStatsCalc.compute(listOf(e(2026, 9, 9_000)), YearMonth.of(2026, 10))!!
        assertEquals(YearMonth.of(2026, 9), s.staleSince)
    }

    @Test
    fun usesOnlyLastSixMonths() {
        val entries = (1..8).map { e(2026, it, it * 1_000L) }
        val s = BillStatsCalc.compute(entries, YearMonth.of(2026, 8))!!
        assertEquals(6, s.months)
        // meses 3..8 -> média de 3000..8000 = 5500
        assertEquals(5_500L, s.averageCents)
    }
}
