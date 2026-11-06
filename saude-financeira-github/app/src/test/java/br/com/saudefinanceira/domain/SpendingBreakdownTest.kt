package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.data.InstallmentEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class SpendingBreakdownTest {
    private val now = YearMonth.of(2026, 10)

    private fun bill(name: String, type: BillType, cents: Long) =
        BillEntity(name = name, type = type, amountCents = cents)

    private fun inst(name: String, total: Long, n: Int, year: Int, month: Int) =
        InstallmentEntity(description = name, totalCents = total, installments = n, firstYear = year, firstMonth = month)

    @Test
    fun sortsBiggestFirstAndMixesKinds() {
        val items = SpendingBreakdown.items(
            bills = listOf(bill("Aluguel", BillType.FIXED, 100_000), bill("Luz", BillType.VARIABLE, 30_000)),
            installments = listOf(inst("Micro-ondas", 60_000, 12, 2026, 6)), // 5.000 por mês, ativa
            now = now,
        )
        assertEquals(listOf("Aluguel", "Luz", "Micro-ondas"), items.map { it.name })
        assertEquals(listOf(SpendKind.FIXED, SpendKind.VARIABLE, SpendKind.INSTALLMENT), items.map { it.kind })
        assertEquals(5_000L, items.last().cents)
    }

    @Test
    fun ignoresFinishedNotStartedAndZeroItems() {
        val items = SpendingBreakdown.items(
            bills = listOf(bill("Zerada", BillType.FIXED, 0)),
            installments = listOf(
                inst("Quitada", 30_000, 3, 2025, 1),
                inst("Futura", 30_000, 3, 2027, 1),
            ),
            now = now,
        )
        assertEquals(emptyList<SpendItem>(), items)
    }
}
