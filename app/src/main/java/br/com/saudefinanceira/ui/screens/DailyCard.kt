package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.saudefinanceira.data.QuickExpenseEntity
import br.com.saudefinanceira.domain.SalarySplit
import br.com.saudefinanceira.domain.SplitPart
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.util.Money

/** Campo simples para anotar o que gastou hoje, com a lista dos gastos de hoje. */
@Composable
fun SpentTodayCard(
    todayExpenses: List<QuickExpenseEntity>,
    onAdd: (Long, String) -> Unit,
    onDelete: (QuickExpenseEntity) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Gastei", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Comprou algo hoje? Anote aqui o valor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MoneyField(amount, { amount = it; error = false }, "Quanto foi? (R$)")
            if (error) Text("Digite só o valor, por exemplo 25,90.", color = MaterialTheme.colorScheme.error)
            Button(
                onClick = {
                    val cents = Money.parse(amount)
                    if (cents == null || cents <= 0L) error = true
                    else {
                        onAdd(cents, "Gasto do dia")
                        amount = ""
                        error = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Gastei", style = MaterialTheme.typography.titleMedium) }

            if (todayExpenses.isNotEmpty()) {
                Text("O que você anotou hoje", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                todayExpenses.forEach { e ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(Money.format(e.amountCents), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { onDelete(e) }) { Text("Apagar") }
                    }
                }
            }
        }
    }
}

/** Para onde vai o dinheiro do mês, em palavras simples. */
@Composable
fun SalaryPlanCard(
    split: SalarySplit,
    incomeCents: Long,
    reserveCents: Long,
    onEditReserve: () -> Unit,
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.primaryContainer,
    )
    fun percent(cents: Long): String = if (incomeCents > 0L) "${(cents * 100 / incomeCents)}%" else ""

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Para onde vai o seu dinheiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Você ganha ${Money.format(incomeCents)} por mês",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val barParts: List<SplitPart> =
                if (split.freeCents > 0L) split.parts + SplitPart("Sobra para o dia a dia", split.freeCents) else split.parts
            Row(
                Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                barParts.forEachIndexed { i, p ->
                    Box(
                        Modifier
                            .weight(p.cents.toFloat().coerceAtLeast(1f))
                            .height(14.dp)
                            .background(if (i < split.parts.size) palette[i % palette.size] else MaterialTheme.colorScheme.primaryContainer)
                    )
                }
            }
            split.parts.forEachIndexed { i, p ->
                SplitRow(p.label, Money.format(p.cents), percent(p.cents), palette[i % palette.size])
            }
            val freeColor = if (split.freeCents < 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
            SplitRow(
                if (split.freeCents < 0L) "Falta para fechar o mês" else "Sobra para o dia a dia",
                Money.format(kotlin.math.abs(split.freeCents)),
                if (split.freeCents < 0L) "" else percent(split.freeCents),
                freeColor,
                bold = true,
            )
            Text(
                if (reserveCents > 0L) "Você separa ${Money.format(reserveCents)} por mês para imprevistos. Isso ajuda a não precisar de empréstimo."
                else "Guardar um pouquinho todo mês ajuda quando aparece um imprevisto. Pode ser pouco, até R$ 10 por semana.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onEditReserve) { Text(if (reserveCents > 0L) "Mudar quanto guardo" else "Quanto posso guardar?") }
        }
    }
}

@Composable
private fun SplitRow(label: String, value: String, percent: String, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        if (percent.isNotEmpty()) Text(percent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ReserveDialog(currentCents: Long, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var text by remember { mutableStateOf(if (currentCents > 0L) Money.formatPlain(currentCents) else "") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quanto posso guardar por mês?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Separe um valor pequeno todo mês para imprevistos. R$ 10 por semana dá cerca de R$ 40 por mês. Deixe vazio se não der agora.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                MoneyField(text, { text = it; error = false }, "Valor por mês (R$)")
                TextButton(onClick = { text = "40,00"; error = false }) { Text("Usar R$ 40,00") }
                if (error) Text("Digite um valor válido, por exemplo 40,00.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isBlank()) onSave(0L)
                else {
                    val cents = Money.parse(text)
                    if (cents == null) error = true else onSave(cents)
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
