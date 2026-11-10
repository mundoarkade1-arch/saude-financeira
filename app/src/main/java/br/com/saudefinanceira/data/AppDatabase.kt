package br.com.saudefinanceira.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Sem fallbackToDestructiveMigration: nunca apagamos dados financeiros em silêncio.
// Ao mudar o schema, aumente a versão e escreva uma Migration.
@Database(
    entities = [IncomeEntity::class, BillEntity::class, InstallmentEntity::class, BillEntryEntity::class, GoalEntity::class, MonthSnapshotEntity::class, DebtEntity::class, QuickExpenseEntity::class],
    version = 8,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): FinanceDao

    companion object {
        /** v1 -> v2: cria o histórico mensal das contas variáveis. Dados existentes são preservados. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `bill_entries` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`billId` INTEGER NOT NULL, " +
                        "`year` INTEGER NOT NULL, " +
                        "`month` INTEGER NOT NULL, " +
                        "`amountCents` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_bill_entries_billId_year_month` " +
                        "ON `bill_entries` (`billId`, `year`, `month`)"
                )
            }
        }

        /** v2 -> v3: cria a tabela de metas. Dados existentes são preservados. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `goals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`targetCents` INTEGER NOT NULL, " +
                        "`savedCents` INTEGER NOT NULL)"
                )
            }
        }

        /** v3 -> v4: cria o histórico mensal. Dados existentes são preservados. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `month_snapshots` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`year` INTEGER NOT NULL, " +
                        "`month` INTEGER NOT NULL, " +
                        "`incomeCents` INTEGER NOT NULL, " +
                        "`fixedCents` INTEGER NOT NULL, " +
                        "`variableCents` INTEGER NOT NULL, " +
                        "`installmentsCents` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_month_snapshots_year_month` " +
                        "ON `month_snapshots` (`year`, `month`)"
                )
            }
        }

        /** v4 -> v5: identifica cartão ou crediário nas compras parceladas. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `installments` ADD COLUMN `paymentMethod` TEXT NOT NULL DEFAULT 'Cartão de crédito'")
            }
        }

        /** v5 -> v6: adiciona o registro local de dívidas, preservando as demais tabelas. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `creditor` TEXT NOT NULL, `description` TEXT NOT NULL, `balanceCents` INTEGER NOT NULL, `monthlyInterestBasisPoints` INTEGER NOT NULL DEFAULT 0, `dueDay` INTEGER, `isNegative` INTEGER NOT NULL DEFAULT 0, `plannedPaymentCents` INTEGER NOT NULL DEFAULT 0)")
            }
        }

        /** v6 -> v7: guarda as parcelas negociadas das dívidas no histórico mensal. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `month_snapshots` ADD COLUMN `debtPaymentsCents` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v7 -> v8: prioridade das contas, dia de recebimento da renda e gastos rápidos. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `bills` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `payDay` INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `quick_expenses` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`amountCents` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`year` INTEGER NOT NULL, " +
                        "`month` INTEGER NOT NULL, " +
                        "`day` INTEGER NOT NULL)"
                )
                // Sugestão inicial para contas que já existiam (a pessoa pode mudar depois).
                val essential = listOf("aluguel", "condom", "luz", "energia", "agua", "água", "gás", "gas", "mercado", "comida", "feira", "remédio", "remedio", "farmácia", "farmacia", "transporte", "passagem", "ônibus", "onibus", "escola", "creche")
                val flexible = listOf("netflix", "spotify", "streaming", "assinatura", "youtube", "amazon", "disney", "hbo")
                db.execSQL("UPDATE `bills` SET `priority` = 0 WHERE " + essential.joinToString(" OR ") { "lower(`name`) LIKE '%$it%'" })
                db.execSQL("UPDATE `bills` SET `priority` = 2 WHERE `priority` = 1 AND (" + flexible.joinToString(" OR ") { "lower(`name`) LIKE '%$it%'" } + ")")
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "saude_financeira.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
    }
}


