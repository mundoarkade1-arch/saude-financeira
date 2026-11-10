package br.com.saudefinanceira.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BillType { FIXED, VARIABLE }

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amountCents: Long,
    /** Dia do mês em que o dinheiro cai na conta (opcional); define o início do ciclo de gastos. */
    val payDay: Int? = null,
)

/** FIXED: valor fixo por tempo indeterminado. VARIABLE: valor do mês mais recente informado. */
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: BillType,
    val amountCents: Long,
    val dueDay: Int? = null,
    /** 0 = essencial, 1 = importante, 2 = pode esperar. Define a ordem de pagamento. */
    @ColumnInfo(defaultValue = "1") val priority: Int = 1,
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
    @ColumnInfo(defaultValue = "'Cartão de crédito'") val paymentMethod: String = "Cartão de crédito",
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

/** Meta de economia (ex.: reserva de emergência). */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetCents: Long,
    val savedCents: Long,
)

/** Resumo de cada mês (o do mês atual é atualizado sempre que os números mudam). */
@Entity(
    tableName = "month_snapshots",
    indices = [Index(value = ["year", "month"], unique = true)],
)
data class MonthSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int,
    val incomeCents: Long,
    val fixedCents: Long,
    val variableCents: Long,
    val installmentsCents: Long,
    @ColumnInfo(defaultValue = "0") val debtPaymentsCents: Long = 0,
)




/** Gasto rápido do dia a dia (mercado, passagem, lanche...), lançado na tela inicial. */
@Entity(tableName = "quick_expenses")
data class QuickExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val note: String,
    val year: Int,
    val month: Int,
    val day: Int,
)

/** Dívida informada pela pessoa; o app não consulta birôs de crédito. */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditor: String,
    val description: String,
    val balanceCents: Long,
    @ColumnInfo(defaultValue = "0") val monthlyInterestBasisPoints: Int = 0,
    val dueDay: Int? = null,
    @ColumnInfo(defaultValue = "0") val isNegative: Boolean = false,
    @ColumnInfo(defaultValue = "0") val plannedPaymentCents: Long = 0,
)
