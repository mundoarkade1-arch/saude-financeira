package br.com.saudefinanceira.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupPlanTest {
    @Test
    fun roundTripsPayDayPriorityAndQuickExpenses() {
        val source = Backup.Data(
            income = listOf(IncomeEntity(1, "Salário", 150_000, 5)),
            bills = listOf(BillEntity(2, "Aluguel", BillType.FIXED, 80_000, 5, 0)),
            installments = emptyList(),
            entries = emptyList(),
            quickExpenses = listOf(QuickExpenseEntity(3, 2_590, "Mercado", 2026, 11, 10)),
        )

        assertEquals(source, Backup.parse(Backup.toJson(source)))
    }

    @Test
    fun olderBackupGetsDefaultPriorityAndNoPayDay() {
        val legacy = """{"app":"saude-financeira","format":1,
            "income":[{"id":1,"description":"Salário","amountCents":150000}],
            "bills":[{"id":2,"name":"Luz","type":"FIXED","amountCents":15000}],
            "installments":[],"entries":[]}"""

        val restored = Backup.parse(legacy)

        assertNull(restored.income.single().payDay)
        assertEquals(1, restored.bills.single().priority)
        assertEquals(emptyList<QuickExpenseEntity>(), restored.quickExpenses)
    }

    @Test
    fun rejectsInvalidQuickExpenseDate() {
        val data = Backup.Data(
            income = emptyList(),
            bills = emptyList(),
            installments = emptyList(),
            entries = emptyList(),
            quickExpenses = listOf(QuickExpenseEntity(1, 1_000, "Lanche", 2026, 2, 30)),
        )

        assertThrows(IllegalArgumentException::class.java) { Backup.parse(Backup.toJson(data)) }
    }
}
