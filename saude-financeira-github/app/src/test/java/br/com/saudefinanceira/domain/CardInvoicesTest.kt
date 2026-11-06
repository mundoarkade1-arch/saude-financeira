package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.InstallmentEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class CardInvoicesTest {
    private val now = YearMonth.of(2026, 10)

    private fun inst(card: String?, total: Long, n: Int, year: Int, month: Int) =
        InstallmentEntity(description = "x", totalCents = total, installments = n, firstYear = year, firstMonth = month, card = card)

    @Test
    fun groupsByCardIgnoringCaseAndSpaces() {
        val list = listOf(
            inst("Nubank", 12_000, 12, 2026, 10),   // 1.000 por mês, por 12 meses
            inst(" nubank ", 6_000, 3, 2026, 10),   // 2.000 por mês, por 3 meses
            inst("Itaú", 9_000, 3, 2027, 1),         // ainda não começou
            inst(null, 30_000, 3, 2025, 1),          // quitada: não aparece
        )
        val result = CardInvoices.compute(list, now)
        assertEquals(listOf("Nubank", "Itaú"), result.map { it.name })

        val nubank = result.first()
        assertEquals(listOf(3_000L, 3_000L, 3_000L, 1_000L), nubank.months)
        assertEquals(YearMonth.of(2027, 9), nubank.lastMonth)

        val itau = result.last()
        assertEquals(listOf(0L, 0L, 0L, 3_000L), itau.months)
    }

    @Test
    fun blankCardUsesDefaultName() {
        val result = CardInvoices.compute(listOf(inst("  ", 10_000, 2, 2026, 10)), now)
        assertEquals(listOf(CardInvoices.NO_CARD), result.map { it.name })
    }
}
