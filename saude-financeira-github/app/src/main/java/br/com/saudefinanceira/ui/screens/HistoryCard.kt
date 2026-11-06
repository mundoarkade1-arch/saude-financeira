package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.saudefinanceira.data.MonthSnapshotEntity
import br.com.saudefinanceira.domain.HistoryCalc
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.ui.theme.BrandGradient
import br.com.saudefinanceira.util.Money
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/** Evolução da sobra nos últimos meses registrados. */
@Composable
fun HistoryCard(entries: List<MonthSnapshotEntity>) {
    val recent = HistoryCalc.recent(entries)
    if (recent.isEmpty()) return

    val maxAbs = recent.maxOf { abs(it.balanceCents) }.coerceAtLeast(1L)
    val barShape = RoundedCornerShape(8.dp)
    val negativeColor = MaterialTheme.colorScheme.error
    val ptBR = Locale("pt", "BR")

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Evolução da sua sobra", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (recent.size < 2) {
                Text(
                    "O histórico começa neste mês. Volte no próximo mês para ver como sua sobra evoluiu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    recent.forEach { m ->
                        val negative = m.balanceCents < 0
                        val barHeight = maxOf(4f, 72f * abs(m.balanceCents).toFloat() / maxAbs).dp
                        val barModifier = if (negative) Modifier.background(negativeColor, barShape)
                        else Modifier.background(BrandGradient, barShape)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((m.balanceCents / 100).toString(), style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(28.dp).height(barHeight).then(barModifier))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                m.month.month.getDisplayName(TextStyle.SHORT, ptBR),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                val delta = HistoryCalc.deltaVsPrevious(recent)
                val previous = monthLabel(recent[recent.size - 2].month)
                if (delta != null) {
                    Text(
                        when {
                            delta > 0L -> "Sua sobra aumentou ${Money.format(delta)} em relação a $previous."
                            delta < 0L -> "Sua sobra diminuiu ${Money.format(-delta)} em relação a $previous."
                            else -> "Sua sobra ficou igual à de $previous."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (delta < 0L) negativeColor else MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "Valores em R$. Cada mês reflete os números da última vez que você abriu o app nele.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
