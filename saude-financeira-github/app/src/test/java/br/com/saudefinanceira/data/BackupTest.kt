package br.com.saudefinanceira.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupTest {
    @Test
    fun roundTripsFinancialDataAndOptionalHistory() {
        val source = Backup.Data(
            income = listOf(IncomeEntity(1, "Salário", 350_000)),
            bills = listOf(BillEntity(2, "Energia", BillType.VARIABLE, 18_250, 12)),
            installments = listOf(InstallmentEntity(3, "Notebook", 240_000, 10, 2026, 1, "Azul")),
            entries = listOf(BillEntryEntity(4, 2, 2026, 2, 18_250)),
            goals = listOf(GoalEntity(5, "Reserva", 1_000_000, 125_000)),
            snapshots = listOf(MonthSnapshotEntity(6, 2026, 2, 350_000, 20_000, 18_250, 24_000)),
        )

        val restored = Backup.parse(Backup.toJson(source))

        assertEquals(source, restored)
    }

    @Test
    fun rejectsHistoryThatReferencesMissingBill() {
        val data = Backup.Data(
            income = emptyList(),
            bills = emptyList(),
            installments = emptyList(),
            entries = listOf(BillEntryEntity(1, 999, 2026, 2, 10_000)),
        )

        assertThrows(IllegalArgumentException::class.java) { Backup.parse(Backup.toJson(data)) }
    }

    @Test
    fun acceptsOlderBackupWithoutGoalsOrSnapshots() {
        val legacy = """{"app":"saude-financeira","format":1,"income":[],"bills":[],"installments":[],"entries":[]}"""

        val restored = Backup.parse(legacy)

        assertEquals(emptyList<GoalEntity>(), restored.goals)
        assertEquals(emptyList<MonthSnapshotEntity>(), restored.snapshots)
    }
}
