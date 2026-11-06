package br.com.saudefinanceira.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceDatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    @Test
    fun savesVariableBillHistoryAndMonthlySnapshot() = runBlocking {
        val billId = db.dao().insertBill(BillEntity(name = "Energia", type = BillType.VARIABLE, amountCents = 18_250))
        db.dao().upsertEntry(BillEntryEntity(billId = billId, year = 2026, month = 2, amountCents = 18_250))
        db.dao().upsertSnapshot(MonthSnapshotEntity(year = 2026, month = 2, incomeCents = 350_000, fixedCents = 100_000, variableCents = 18_250, installmentsCents = 24_000))

        assertEquals(1, db.dao().getEntries().size)
        assertEquals(18_250L, db.dao().getEntries().single().amountCents)
        assertEquals(1, db.dao().getSnapshots().size)
    }

    @Test
    fun backupRestoreReplacesAllTablesAtomically() = runBlocking {
        db.dao().upsertIncome(IncomeEntity(id = 1, description = "Antiga", amountCents = 500_000))
        val data = Backup.Data(
            income = listOf(IncomeEntity(id = 7, description = "Salário", amountCents = 350_000)),
            bills = listOf(BillEntity(id = 8, name = "Água", type = BillType.VARIABLE, amountCents = 12_500)),
            installments = emptyList(),
            entries = listOf(BillEntryEntity(id = 9, billId = 8, year = 2026, month = 2, amountCents = 12_500)),
            goals = listOf(GoalEntity(id = 10, name = "Reserva", targetCents = 1_000_000, savedCents = 100_000)),
            snapshots = listOf(MonthSnapshotEntity(id = 11, year = 2026, month = 2, incomeCents = 350_000, fixedCents = 0, variableCents = 12_500, installmentsCents = 0)),
        )

        Backup.replaceAll(db, data)

        assertEquals(data, Backup.snapshot(db))
    }
}
