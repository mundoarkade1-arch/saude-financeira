package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.InstallmentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class InstallmentCalcTest {
    private fun item(total: Long, n: Int, year: Int, month: Int) =
        InstallmentEntity(description = "x", totalCents = total, installments = n, firstYear = year, firstMonth = month)

    @Test
    fun middleOfPlan() {
        val st = InstallmentCalc.status(item(120_000, 12, 2026, 6), YearMonth.of(2026, 10))
        assertEquals(5, st.current)
        assertEquals(50_000L, st.paidCents)
        assertEquals(70_000L, st.remainingCents)
        assertEquals(10_000L, st.monthlyCents)
        assertFalse(st.notStarted)
        assertFalse(st.finished)
    }

    @Test
    fun notStartedYet() {
        val st = InstallmentCalc.status(item(120_000, 12, 2027, 1), YearMonth.of(2026, 10))
        assertTrue(st.notStarted)
        assertEquals(0, st.current)
        assertEquals(0L, st.monthlyCents)
        assertEquals(120_000L, st.remainingCents)
    }

    @Test
    fun finished() {
        val st = InstallmentCalc.status(item(30_000, 3, 2025, 1), YearMonth.of(2026, 10))
        assertTrue(st.finished)
        assertEquals(3, st.current)
        assertEquals(0L, st.remainingCents)
        assertEquals(0L, st.monthlyCents)
    }

    @Test
    fun remainderGoesToFirstParcels() {
        val parts = (1..3).map { InstallmentCalc.parcelAmount(1000, 3, it) }
        assertEquals(listOf(334L, 333L, 333L), parts)
        assertEquals(1000L, parts.sum())
    }

    @Test
    fun lastParcelMonth() {
        val st = InstallmentCalc.status(item(120_000, 12, 2026, 1), YearMonth.of(2026, 12))
        assertEquals(12, st.current)
        assertFalse(st.finished)
        assertEquals(0L, st.remainingCents)
    }
}
