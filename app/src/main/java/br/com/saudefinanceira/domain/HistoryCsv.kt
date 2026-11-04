package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.MonthSnapshotEntity
import java.time.YearMonth
import kotlin.math.absoluteValue

/** CSV com separador e decimais compatíveis com planilhas configuradas em português. */
object HistoryCsv {
    fun export(entries: List<MonthSnapshotEntity>, from: YearMonth, through: YearMonth): String {
        require(!through.isBefore(from)) { "O período final deve ser igual ou posterior ao inicial." }
        val months = entries
            .filter { YearMonth.of(it.year, it.month) in from..through }
            .sortedWith(compareBy<MonthSnapshotEntity> { it.year }.thenBy { it.month })
        return buildString {
            append('\uFEFF')
            appendLine("Mês;Renda;Contas fixas;Contas variáveis;Parcelas;Gastos totais;Sobra")
            months.forEach { item ->
                val total = item.fixedCents + item.variableCents + item.installmentsCents
                val balance = item.incomeCents - total
                append(YearMonth.of(item.year, item.month)).append(';')
                    .append(decimal(item.incomeCents)).append(';')
                    .append(decimal(item.fixedCents)).append(';')
                    .append(decimal(item.variableCents)).append(';')
                    .append(decimal(item.installmentsCents)).append(';')
                    .append(decimal(total)).append(';')
                    .appendLine(decimal(balance))
            }
        }
    }

    private fun decimal(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val absolute = cents.absoluteValue
        return "$sign${absolute / 100},${(absolute % 100).toString().padStart(2, '0')}"
    }
}
