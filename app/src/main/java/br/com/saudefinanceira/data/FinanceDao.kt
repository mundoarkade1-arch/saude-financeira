package br.com.saudefinanceira.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // ---- observação (telas) ----
    @Query("SELECT * FROM income")
    fun observeIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM bills ORDER BY name")
    fun observeBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM installments ORDER BY description")
    fun observeInstallments(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM bill_entries")
    fun observeEntries(): Flow<List<BillEntryEntity>>

    // ---- escrita ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIncome(income: IncomeEntity)

    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: BillEntryEntity)

    @Query("DELETE FROM bill_entries WHERE billId = :billId")
    suspend fun deleteEntriesOf(billId: Long)

    @Insert
    suspend fun insertInstallment(item: InstallmentEntity): Long

    @Update
    suspend fun updateInstallment(item: InstallmentEntity)

    @Delete
    suspend fun deleteInstallment(item: InstallmentEntity)

    // ---- backup ----
    @Query("SELECT * FROM income")
    suspend fun getIncome(): List<IncomeEntity>

    @Query("SELECT * FROM bills")
    suspend fun getBills(): List<BillEntity>

    @Query("SELECT * FROM installments")
    suspend fun getInstallments(): List<InstallmentEntity>

    @Query("SELECT * FROM bill_entries")
    suspend fun getEntries(): List<BillEntryEntity>

    @Query("DELETE FROM income")
    suspend fun clearIncome()

    @Query("DELETE FROM bills")
    suspend fun clearBills()

    @Query("DELETE FROM installments")
    suspend fun clearInstallments()

    @Query("DELETE FROM bill_entries")
    suspend fun clearEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(list: List<IncomeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(list: List<BillEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(list: List<InstallmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(list: List<BillEntryEntity>)

    // ---- metas ----
    @Query("SELECT * FROM goals ORDER BY id")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals")
    suspend fun getGoals(): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(list: List<GoalEntity>)

    // ---- histórico mensal ----
    @Query("SELECT * FROM month_snapshots")
    fun observeSnapshots(): Flow<List<MonthSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSnapshot(snapshot: MonthSnapshotEntity)

    @Query("SELECT * FROM month_snapshots")
    suspend fun getSnapshots(): List<MonthSnapshotEntity>

    @Query("DELETE FROM month_snapshots")
    suspend fun clearSnapshots()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(list: List<MonthSnapshotEntity>)
}
