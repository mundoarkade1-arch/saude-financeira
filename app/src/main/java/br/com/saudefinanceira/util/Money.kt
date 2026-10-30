package br.com.saudefinanceira.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/** Valores monetários são guardados em centavos (Long) para evitar erros de arredondamento. */
object Money {
    private val locale = Locale("pt", "BR")
    private const val MAX_CENTS = 10_000_000_000_000L

    /** "3.500", "12.345.678": ponto como separador de milhar (padrão brasileiro). */
    private val THOUSANDS = Regex("^[1-9]\\d{0,2}(\\.\\d{3})+$")
    private val DECIMAL = Regex("^(\\d+\\.?\\d*|\\.\\d+)$")

    fun format(cents: Long): String =
        NumberFormat.getCurrencyInstance(locale).format(cents / 100.0)

    /** Texto editável, ex.: 350050 -> "3500,50". */
    fun formatPlain(cents: Long): String =
        String.format(locale, "%d,%02d", cents / 100, cents % 100)

    /**
     * Converte texto digitado em centavos. Aceita "3.500,50", "3500,50", "3500.50", "3.500" (= 3500 reais),
     * "R$ 12,5". Retorna null se for inválido, negativo ou grande demais.
     */
    fun parse(text: String): Long? {
        var clean = text.replace("R$", "").filterNot { it.isWhitespace() || it == '\u00A0' }
        if (clean.isEmpty()) return null
        clean = when {
            clean.contains(',') -> clean.replace(".", "").replace(',', '.')
            THOUSANDS.matches(clean) -> clean.replace(".", "")
            else -> clean
        }
        if (!DECIMAL.matches(clean)) return null
        val cents = try {
            BigDecimal(clean).movePointRight(2).setScale(0, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            return null
        }
        if (cents > BigDecimal.valueOf(MAX_CENTS)) return null
        return cents.toLong()
    }
}
