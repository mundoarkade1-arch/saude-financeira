package br.com.saudefinanceira

import android.app.Application
import br.com.saudefinanceira.data.AppDatabase

class FinanceApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.create(this) }
}
