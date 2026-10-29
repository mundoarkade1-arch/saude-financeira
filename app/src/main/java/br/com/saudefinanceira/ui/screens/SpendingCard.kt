package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.saudefinanceira.domain.Snapshot
import br.com.saudefinanceira.domain.SpendItem
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.GradientProgress
import br.com.saudefinanceira.ui.theme.Blue
import br.com.saudefinanceira.ui.theme.Teal
import br.com.saudefinanceira.util.Money

private val Amber = Color(0xFFF5A623)

/** "Para onde vai seu dinheiro": composição do mês e maiores gastos. */
@Composable
fun SpendingCard(s: Snapshot, items: List<SpendItem>) {
    if (items.isEmpty()) return

    val base = maxOf(s.incomeCents, s.spentCents).coerceAtLeast(1L)
    val remaining = (s.incomeCents - s.spentCents).coerceAtLeast(0L)
    val segments = listOf(
        Triple("Fixas", s.fixedCents, Teal),
        Triple("Variáveis", s.variableCents, Blue),
        Triple("Parcelas", s.installmentsCents, Amber),
        Triple("Sobra", remaining, MaterialTheme.colorScheme.surfaceVariant),
    ).filter { it.second > 0L }

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Para onde vai seu dinheiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50))) {
                segments.forEach { (_, value, color) ->
                    Box(Modifier.weight(value.toFloat() / base).fillMaxHeight().background(color))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                segments.forEach { (label, value, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Text(
                            "$label: ${Money.format(value)} (${value * 100 / base}%)",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Text("Maiores gastos do mês", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            val biggest = items.first().cents.toFloat()
            items.take(5).forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            Money.format(item.cents) + if (s.incomeCents > 0L) " · ${item.cents * 100 / s.incomeCents}%" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    GradientProgress(item.cents / biggest, height = 6.dp)
                }
            }
        }
    }
}
