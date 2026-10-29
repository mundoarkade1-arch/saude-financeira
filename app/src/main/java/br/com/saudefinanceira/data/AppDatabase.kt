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
    entities = [IncomeEntity::class, BillEntity::class, InstallmentEntity::class, BillEntryEntity::class],
    version = 2,
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

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "saude_financeira.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
