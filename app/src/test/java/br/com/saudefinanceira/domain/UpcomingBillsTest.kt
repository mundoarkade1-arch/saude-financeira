package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class UpcomingBillsTest {
    private val today = LocalDate.of(2026, 11, 10)

    private fun bill(name: String, day: Int?) =
        BillEntity(name = name, type = BillType.FIXED, amountCents = 10_000, dueDay = day)

    @Test
    fun countsDaysUntilNextDueDate() {
        assertEquals(0, UpcomingBills.daysUntil(10, today))
        assertEquals(2, UpcomingBills.daysUntil(12, today))
        assertEquals(25, UpcomingBills.daysUntil(5, today))
    }

    @Test
    fun shortMonthsUseTheLastDay() {
        assertEquals(0, UpcomingBills.daysUntil(31, LocalDate.of(2026, 11, 30)))
    }

    @Test
    fun listsOnlyBillsDueSoonInOrder() {
        val list = UpcomingBills.next(
            listOf(bill("Internet", 15), bill("Luz", 12), bill("Aluguel", 10), bill("Sem data", null), bill("Longe", 5)),
            today,
        )
        assertEquals(listOf("Aluguel", "Luz", "Internet"), list.map { it.bill.name })
    }
}
