package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntryEntity
import java.time.YearMonth

data class BillStats(
    val averageCents: Long,
    val months: Int,
    /** Variação do valor mais recente em relação à média dos meses anteriores (null se não há base). */
    val trendPercent: Int?,
    /** Mês do último valor informado, se for anterior ao mês atual (valor desatualizado). */
    val staleSince: YearMonth?,
)

object BillStatsCalc {
    fun compute(entries: List<BillEntryEntity>, now: YearMonth): BillStats? {
        if (entries.isEmpty()) return null
        val sorted = entries
            .sortedWith(compareByDescending<BillEntryEntity> { it.year }.thenByDescending { it.month })
            .take(6)
        val average = sorted.sumOf { it.amountCents } / sorted.size
        val latest = sorted.first()
        val previous = sorted.drop(1)
        val trend: Int? = if (previous.isEmpty()) null else {
            val prevAvg = previous.sumOf { it.amountCents } / previous.size
            if (prevAvg <= 0L) null else Math.round((latest.amountCents - prevAvg) * 100.0 / prevAvg).toInt()
        }
        val latestMonth = YearMonth.of(latest.year, latest.month)
        return BillStats(average, sorted.size, trend, if (latestMonth.isBefore(now)) latestMonth else null)
    }
}
