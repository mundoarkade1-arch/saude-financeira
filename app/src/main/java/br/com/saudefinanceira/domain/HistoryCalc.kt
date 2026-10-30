package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.MonthSnapshotEntity
import java.time.YearMonth

data class MonthBalance(val month: YearMonth, val balanceCents: Long)

object HistoryCalc {
    fun balanceOf(e: MonthSnapshotEntity): Long =
        e.incomeCents - e.fixedCents - e.variableCents - e.installmentsCents

    /** Os últimos [limit] meses registrados, em ordem cronológica. */
    fun recent(entries: List<MonthSnapshotEntity>, limit: Int = 6): List<MonthBalance> =
        entries
            .sortedWith(compareByDescending<MonthSnapshotEntity> { it.year }.thenByDescending { it.month })
            .take(limit)
            .reversed()
            .map { MonthBalance(YearMonth.of(it.year, it.month), balanceOf(it)) }

    /** Diferença entre o mês mais recente e o registrado antes dele (null se só há um mês). */
    fun deltaVsPrevious(recent: List<MonthBalance>): Long? =
        if (recent.size < 2) null else recent[recent.size - 1].balanceCents - recent[recent.size - 2].balanceCents
}
