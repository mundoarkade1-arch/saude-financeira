package br.com.saudefinanceira.reminders

import android.content.Context
import java.time.LocalDate

/** Preferências dos lembretes (guardadas só no aparelho). */
class ReminderPrefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(value) { sp.edit().putBoolean("enabled", value).apply() }

    /** Quantos dias antes do vencimento avisar (0 = só no dia). */
    var leadDays: Int
        get() = sp.getInt("leadDays", 1)
        set(value) { sp.edit().putInt("leadDays", value).apply() }

    /** Registra a chave e devolve true se ela ainda não tinha sido avisada. Limpa chaves com mais de 60 dias. */
    fun markIfNew(key: String, today: LocalDate): Boolean {
        val set = (sp.getStringSet("notified", null) ?: emptySet()).toMutableSet()
        if (key in set) return false
        set.removeAll { old ->
            try {
                LocalDate.parse(old.substringAfter('|')).isBefore(today.minusDays(60))
            } catch (e: Exception) {
                true
            }
        }
        set.add(key)
        sp.edit().putStringSet("notified", set).apply()
        return true
    }
}
