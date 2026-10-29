package br.com.saudefinanceira.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BillType { FIXED, VARIABLE }

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amountCents: Long,
)

/** FIXED: valor fixo por tempo indeterminado. VARIABLE: valor do mês mais recente informado. */
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: BillType,
    val amountCents: Long,
    val dueDay: Int? = null,
)

/** O app calcula a parcela atual a partir do mês da primeira parcela. */
@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val totalCents: Long,
    val installments: Int,
    val firstYear: Int,
    val firstMonth: Int,
    val card: String? = null,
)

/** Histórico mensal das contas variáveis (uma linha por conta e mês). */
@Entity(
    tableName = "bill_entries",
    indices = [Index(value = ["billId", "year", "month"], unique = true)],
)
data class BillEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val year: Int,
    val month: Int,
    val amountCents: Long,
)
