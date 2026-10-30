package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.data.InstallmentEntity
import br.com.saudefinanceira.domain.InstallmentCalc
import br.com.saudefinanceira.domain.SpendingBreakdown
import br.com.saudefinanceira.domain.TipEngine
import br.com.saudefinanceira.domain.TipLevel
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.HeaderProgress
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.ui.theme.BrandGradient
import br.com.saudefinanceira.update.UpdateInfo
import br.com.saudefinanceira.util.Money
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val ptBR = Locale("pt", "BR")

@Composable
fun HomeScreen(
    vm: FinanceViewModel,
    padding: PaddingValues,
    onOpenTips: () -> Unit,
    pendingUpdate: UpdateInfo?,
    onOpenSettings: () -> Unit,
    onOpenBills: () -> Unit = {},
    onOpenInstallments: () -> Unit = {},
) {
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()
    val bills by vm.bills.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    val now = YearMonth.now()
    val monthName = now.month.getDisplayName(TextStyle.FULL, ptBR)
    val committed = if (s.incomeCents > 0) s.spentCents.toFloat() / s.incomeCents else 0f
    val rising by vm.risingBills.collectAsStateWithLifecycle()
    val tips = TipEngine.generate(s, rising)
    val highlight = tips.firstOrNull { it.level != TipLevel.INFO } ?: tips.first()

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Sobra prevista de $monthName",
            value = Money.format(s.balanceCents),
            caption = if (s.incomeCents == 0L) "Informe seu salário para começar"
            else "${(committed * 100).toInt()}% da renda comprometida",
        ) {
            if (s.incomeCents > 0) {
                Spacer(Modifier.height(12.dp))
                HeaderProgress(committed)
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (pendingUpdate != null) {
                AppCard(Modifier.fillMaxWidth(), onClick = onOpenSettings) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nova versão disponível", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${pendingUpdate.tag} — toque aqui para atualizar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (s.incomeCents == 0L || bills.isEmpty()) {
                OnboardingCard(
                    hasSalary = s.incomeCents > 0L,
                    hasBills = bills.isNotEmpty(),
                    hasInstallments = installments.isNotEmpty(),
                    onSalary = { showDialog = true },
                    onBills = onOpenBills,
                    onInstallments = onOpenInstallments,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Fixas", Money.format(s.fixedCents), Modifier.weight(1f))
                StatCard("Variáveis", Money.format(s.variableCents), Modifier.weight(1f))
                StatCard("Parcelas", Money.format(s.installmentsCents), Modifier.weight(1f))
            }

            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(highlight.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(highlight.message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onOpenTips) { Text("Ver todas as dicas") }
                }
            }

            SpendingCard(s, SpendingBreakdown.items(bills, installments, now))

            HistoryCard(history)

            ProjectionCard(installments, now)

            Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.incomeCents == 0L) "Informar salário" else "Alterar salário (${Money.format(s.incomeCents)})")
            }
        }
    }

    if (showDialog) {
        SalaryDialog(
            initialCents = s.incomeCents,
            onDismiss = { showDialog = false },
            onSave = { vm.saveSalary(it); showDialog = false },
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProjectionCard(installments: List<InstallmentEntity>, now: YearMonth) {
    val months = (0..5).map { now.plusMonths(it.toLong()) }
    val totals = months.map { InstallmentCalc.monthlyTotal(installments, it) }
    val max = (totals.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val last = InstallmentCalc.lastMonth(installments)
    val barShape = RoundedCornerShape(8.dp)
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parcelas nos próximos meses (R$)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                months.forEachIndexed { i, m ->
                    val zero = totals[i] == 0L
                    val frac = totals[i].toFloat() / max
                    val barHeight = maxOf(4f, 72f * frac).dp
                    val barModifier = if (zero) Modifier.background(emptyColor, barShape)
                    else Modifier.background(BrandGradient, barShape)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (zero) "–" else (totals[i] / 100).toString(), style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.width(28.dp).height(barHeight).then(barModifier))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            m.month.getDisplayName(TextStyle.SHORT, ptBR),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                if (last != null && !last.isBefore(now)) "Sua última parcela vence em ${monthLabel(last)}."
                else "Você não tem parcelas pendentes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SalaryDialog(initialCents: Long, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var text by remember { mutableStateOf(if (initialCents > 0) Money.formatPlain(initialCents) else "") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seu salário mensal") },
        text = {
            Column {
                MoneyField(value = text, onValueChange = { text = it; error = false }, label = "Valor (R$)")
                if (error) {
                    Text("Digite um valor válido, ex.: 3500,00", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(text)
                if (cents == null) error = true else onSave(cents)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun OnboardingCard(
    hasSalary: Boolean,
    hasBills: Boolean,
    hasInstallments: Boolean,
    onSalary: () -> Unit,
    onBills: () -> Unit,
    onInstallments: () -> Unit,
) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Primeiros passos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            StepRow(hasSalary, "Informe seu salário", "Informar", onSalary)
            StepRow(hasBills, "Cadastre suas contas (aluguel, luz, mercado...)", "Cadastrar", onBills)
            StepRow(hasInstallments, "Cadastre compras parceladas (se tiver)", "Cadastrar", onInstallments)
        }
    }
}

@Composable
private fun StepRow(done: Boolean, text: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (done) "✓" else "•",
            style = MaterialTheme.typography.titleMedium,
            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (!done) TextButton(onClick = onClick) { Text(action) }
    }
}
