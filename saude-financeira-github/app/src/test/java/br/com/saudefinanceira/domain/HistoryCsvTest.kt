package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.MonthSnapshotEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class HistoryCsvTest {
    @Test
    fun exportsInclusiveRangeWithBalanceAndPortugueseDecimals() {
        val entries = listOf(
            MonthSnapshotEntity(year = 2026, month = 1, incomeCents = 500_000, fixedCents = 100_000, variableCents = 50_050, installmentsCents = 25_000),
            MonthSnapshotEntity(year = 2026, month = 2, incomeCents = 600_000, fixedCents = 150_000, variableCents = 75_000, installmentsCents = 25_000),
            MonthSnapshotEntity(year = 2026, month = 3, incomeCents = 700_000, fixedCents = 100_000, variableCents = 0, installmentsCents = 0),
        )

        val csv = HistoryCsv.export(entries, YearMonth.of(2026, 1), YearMonth.of(2026, 2))

        assertTrue(csv.startsWith("\uFEFFMês;Renda;"))
        assertTrue(csv.contains("2026-01;5000,00;1000,00;500,50;250,00;0,00;1750,50;3249,50"))
        assertTrue(csv.contains("2026-02;6000,00;1500,00;750,00;250,00;0,00;2500,00;3500,00"))
        assertFalse(csv.contains("2026-03"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsReversedDateRange() {
        HistoryCsv.export(emptyList(), YearMonth.of(2026, 3), YearMonth.of(2026, 2))
    }
}
