package br.com.saudefinanceira.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM income")
    fun observeIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM bills ORDER BY name")
    fun observeBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM installments ORDER BY description")
    fun observeInstallments(): Flow<List<InstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIncome(income: IncomeEntity)

    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Insert
    suspend fun insertInstallment(item: InstallmentEntity): Long

    @Delete
    suspend fun deleteInstallment(item: InstallmentEntity)
}
