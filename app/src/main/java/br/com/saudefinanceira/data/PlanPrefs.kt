package br.com.saudefinanceira.data

import android.content.Context

/** Preferências do plano mensal (guardadas só no aparelho). */
class PlanPrefs(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("plano", Context.MODE_PRIVATE)

    /** Quanto a pessoa quer separar por mês como reserva mínima, em centavos. */
    var reserveCents: Long
        get() = sp.getLong("reserveCents", 0L).coerceAtLeast(0L)
        set(value) { sp.edit().putLong("reserveCents", value.coerceAtLeast(0L)).apply() }
}
