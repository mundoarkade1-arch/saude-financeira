package br.com.saudefinanceira.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** Valores monetários são guardados em centavos (Long) para evitar erros de arredondamento. */
object Money {
    private val locale = Locale("pt", "BR")

    fun format(cents: Long): String =
        NumberFormat.getCurrencyInstance(locale).format(cents / 100.0)

    /** Texto editável, ex.: 350050 -> "3500,50". */
    fun formatPlain(cents: Long): String =
        String.format(locale, "%d,%02d", cents / 100, cents % 100)

    /** Converte "3.500,50", "3500,50" ou "3500.50" em centavos. Retorna null se inválido. */
    fun parse(text: String): Long? {
        var clean = text.trim().replace("R$", "").replace(" ", "")
        if (clean.isEmpty()) return null
        clean = if (clean.contains(',')) clean.replace(".", "").replace(',', '.') else clean
        return try {
            val cents = BigDecimal(clean).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
            if (cents < 0) null else cents
        } catch (e: NumberFormatException) {
            null
        }
    }
}
