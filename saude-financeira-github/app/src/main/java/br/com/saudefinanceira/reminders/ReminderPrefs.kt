package br.com.saudefinanceira.reminders

import android.content.Context
import java.time.LocalDate

/** Preferências dos lembretes (guardadas só no aparelho). */
class ReminderPrefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", false)
        set(value) { sp.edit().putBoolean("enabled", value).apply() }

    /** Dias exatos em que o app avisa: 5, 2 e 0 (dia do vencimento). */
    var alertDays: Set<Int>
        get() = (sp.getStringSet("alertDays", null) ?: setOf("5", "2", "0"))
            .mapNotNull { it.toIntOrNull() }.filter { it in 0..31 }.toSet()
        set(value) { sp.edit().putStringSet("alertDays", value.map(Int::toString).toSet()).apply() }

    /** Registra a chave e devolve true se ela ainda não tinha sido avisada. Limpa chaves com mais de 60 dias. */
    fun markIfNew(key: String, today: LocalDate): Boolean {
        val set = (sp.getStringSet("notified", null) ?: emptySet()).toMutableSet()
        if (key in set) return false
        set.removeAll { old ->
            try {
                LocalDate.parse(old.split('|').getOrNull(1) ?: "").isBefore(today.minusDays(60))
            } catch (e: Exception) {
                true
            }
        }
        set.add(key)
        sp.edit().putStringSet("notified", set).apply()
        return true
    }
}

