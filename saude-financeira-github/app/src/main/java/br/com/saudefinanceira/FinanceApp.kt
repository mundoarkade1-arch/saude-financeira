package br.com.saudefinanceira

import android.app.Application
import br.com.saudefinanceira.data.AppDatabase
import br.com.saudefinanceira.reminders.ReminderPrefs
import br.com.saudefinanceira.reminders.ReminderScheduler

class FinanceApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.create(this) }

    override fun onCreate() {
        super.onCreate()
        // Garante que o agendamento exista (por exemplo, depois de reiniciar o aparelho ou atualizar o app).
        if (ReminderPrefs(this).enabled) ReminderScheduler.schedule(this)
    }
}
