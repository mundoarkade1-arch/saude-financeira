package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentPlanTest {
    private fun bill(name: String, cents: Long, day: Int?, priority: Int) =
        BillEntity(name = name, type = BillType.FIXED, amountCents = cents, dueDay = day, priority = priority)

    private val bills = listOf(
        bill("Netflix", 4_000, 1, Priority.FLEXIBLE),
        bill("Internet", 10_000, 15, Priority.IMPORTANT),
        bill("Luz", 15_000, 10, Priority.ESSENTIAL),
        bill("Aluguel", 80_000, 5, Priority.ESSENTIAL),
    )

    @Test
    fun ordersByPriorityThenDueDay() {
        assertEquals(listOf("Aluguel", "Luz", "Internet", "Netflix"), PaymentPlan.order(bills).map { it.name })
    }

    @Test
    fun coversBillsInOrderUntilMoneyRunsOut() {
        val plan = PaymentPlan.build(bills, 100_000)
        assertEquals(listOf(true, true, false, false), plan.steps.map { it.covered })
        assertEquals(5_000, plan.steps[2].missingCents)
        assertEquals(4_000, plan.steps[3].missingCents)
        assertEquals(2, plan.coveredCount)
        assertEquals(2, plan.uncoveredCount)
    }

    @Test
    fun reportsLeftoverWhenEverythingIsCovered() {
        val plan = PaymentPlan.build(bills, 200_000)
        assertTrue(plan.steps.all { it.covered })
        assertEquals(91_000, plan.leftoverCents)
    }

    @Test
    fun negativeMoneyCoversNothing() {
        val plan = PaymentPlan.build(bills, -5_000)
        assertFalse(plan.steps.any { it.covered })
    }

    @Test
    fun suggestsPriorityFromName() {
        assertEquals(Priority.ESSENTIAL, Priority.suggest("Aluguel"))
        assertEquals(Priority.ESSENTIAL, Priority.suggest("Conta de Luz"))
        assertEquals(Priority.FLEXIBLE, Priority.suggest("Netflix"))
        assertEquals(Priority.IMPORTANT, Priority.suggest("Internet"))
        assertEquals(Priority.IMPORTANT, Priority.suggest(""))
    }

    @Test
    fun salarySplitSeparatesPrioritiesAndLeavesTheRest() {
        val s = Snapshot(incomeCents = 150_000, fixedCents = 94_000, variableCents = 0, installmentsCents = 20_000)
        val split = SalarySplitCalc.compute(bills.filter { it.name != "Luz" }, s, 4_000)
        assertEquals(
            listOf("Contas essenciais", "Contas importantes", "Contas que podem esperar", "Parcelas", "Reserva mínima"),
            split.parts.map { it.label },
        )
        assertEquals(80_000, split.parts[0].cents)
        assertEquals(32_000, split.freeCents)
    }
}
