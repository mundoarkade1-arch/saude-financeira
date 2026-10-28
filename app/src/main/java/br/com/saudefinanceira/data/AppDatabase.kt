package br.com.saudefinanceira.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Sem fallbackToDestructiveMigration: nunca apagamos dados financeiros em silêncio.
// Ao mudar o schema, aumente a versão e escreva uma Migration.
@Database(
    entities = [IncomeEntity::class, BillEntity::class, InstallmentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): FinanceDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "saude_financeira.db").build()
    }
}
