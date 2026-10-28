package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.InstallmentEntity
import java.time.YearMonth

data class InstallmentStatus(
    val current: Int,
    val total: Int,
    val paidCents: Long,
    val remainingCents: Long,
    val monthlyCents: Long,
    val notStarted: Boolean,
    val finished: Boolean,
)

object InstallmentCalc {
    /** Valor da parcela [index] (1..n). O resto da divisão vai para as primeiras parcelas. */
    fun parcelAmount(totalCents: Long, n: Int, index: Int): Long {
        val base = totalCents / n
        val rem = (totalCents % n).toInt()
        return base + if (index <= rem) 1 else 0
    }

    /** Soma das [k] primeiras parcelas. */
    fun paidAfter(totalCents: Long, n: Int, k: Int): Long {
        val kk = k.coerceIn(0, n)
        val base = totalCents / n
        val rem = (totalCents % n).toInt()
        return base * kk + minOf(kk, rem)
    }

    /** A parcela do mês [now] é a de número (meses desde a primeira) + 1. */
    fun status(item: InstallmentEntity, now: YearMonth): InstallmentStatus {
        val n = item.installments
        val elapsed = (now.year - item.firstYear) * 12 + (now.monthValue - item.firstMonth)
        val k = (elapsed + 1).coerceIn(0, n)
        val paid = paidAfter(item.totalCents, n, k)
        val active = elapsed >= 0 && elapsed < n
        return InstallmentStatus(
            current = k,
            total = n,
            paidCents = paid,
            remainingCents = item.totalCents - paid,
            monthlyCents = if (active) parcelAmount(item.totalCents, n, elapsed + 1) else 0L,
            notStarted = elapsed < 0,
            finished = elapsed >= n,
        )
    }

    fun monthlyTotal(items: List<InstallmentEntity>, month: YearMonth): Long =
        items.sumOf { status(it, month).monthlyCents }
}
