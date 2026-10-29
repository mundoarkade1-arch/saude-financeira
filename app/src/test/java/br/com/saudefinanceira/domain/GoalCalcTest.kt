package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.GoalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class GoalCalcTest {
    private fun goal(target: Long, saved: Long) = GoalEntity(name = "x", targetCents = target, savedCents = saved)
    private val now = YearMonth.of(2026, 10)

    @Test
    fun estimatesCompletionMonth() {
        val p = GoalCalc.progress(goal(12_000, 2_000), 2_500, now)
        assertEquals(10_000L, p.remainingCents)
        assertEquals(YearMonth.of(2027, 2), p.eta) // 4 meses
        assertFalse(p.done)
        assertEquals(2_000f / 12_000f, p.fraction, 0.0001f)
    }

    @Test
    fun doneWhenSavedReachesTarget() {
        val p = GoalCalc.progress(goal(10_000, 12_000), 1_000, now)
        assertTrue(p.done)
        assertEquals(0L, p.remainingCents)
        assertEquals(1f, p.fraction, 0.0001f)
        assertNull(p.eta)
    }

    @Test
    fun noEtaWithoutPositivePace() {
        assertNull(GoalCalc.progress(goal(10_000, 0), 0, now).eta)
        assertNull(GoalCalc.progress(goal(10_000, 0), -500, now).eta)
    }

    @Test
    fun noEtaWhenTooFarAway() {
        assertNull(GoalCalc.progress(goal(10_000_000, 0), 1, now).eta)
    }
}
