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
    entities = [IncomeEntity::class, BillEntity::class, InstallmentEntity::class, BillEntryEntity::class, GoalEntity::class, MonthSnapshotEntity::class],
    version = 4,
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

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "saude_financeira.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}
