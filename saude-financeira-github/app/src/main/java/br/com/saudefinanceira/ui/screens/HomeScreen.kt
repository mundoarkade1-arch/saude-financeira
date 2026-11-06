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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import br.com.saudefinanceira.data.IncomeEntity
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
import br.com.saudefinanceira.update.UpdateState
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
    updateState: UpdateState = UpdateState.Idle,
    onCheckUpdates: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenBills: () -> Unit = {},
    onOpenInstallments: () -> Unit = {},
    onOpenReports: () -> Unit = {},
) {
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val incomes by vm.incomes.collectAsStateWithLifecycle()
    val debts by vm.debts.collectAsStateWithLifecycle()
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
            caption = if (s.incomeCents == 0L && s.benefitIncomeCents > 0L) "VR/VM estão separados · informe também sua renda em dinheiro"
            else if (s.incomeCents == 0L) "Informe sua renda em dinheiro para começar"
            else "${(committed * 100).toInt()}% da renda em dinheiro comprometida" + if (s.benefitIncomeCents > 0) " · VR/VM acompanhados à parte" else "",
        ) {
            if (s.incomeCents > 0) {
                Spacer(Modifier.height(12.dp))
                HeaderProgress(committed)
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when {
                pendingUpdate != null -> AppCard(Modifier.fillMaxWidth(), onClick = onOpenSettings) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nova versão disponível", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${pendingUpdate.tag} — toque aqui para atualizar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                updateState is UpdateState.Checking -> AppCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(Modifier.width(22.dp).height(22.dp), strokeWidth = 2.dp)
                        Text("Verificando atualizações publicadas…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                updateState is UpdateState.UpToDate -> AppCard(Modifier.fillMaxWidth(), onClick = onOpenSettings) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("App atualizado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Nenhuma versão mais recente foi publicada nas Releases. APKs em Actions são apenas para teste.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                updateState is UpdateState.Failed -> AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Não foi possível verificar atualizações", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(updateState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onCheckUpdates) { Text("Tentar novamente") }
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

            SpendingCard(s, SpendingBreakdown.items(bills, installments, now, debts))

            HistoryCard(history)

            AppCard(Modifier.fillMaxWidth(), onClick = onOpenReports) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Relatórios por período", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Compare os meses e exporte para planilha.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            ProjectionCard(installments, now)

            Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Organizar renda e benefícios (${Money.format(s.incomeCents)})")
            }
        }
    }

    if (showDialog) {
        IncomeDialog(
            incomes = incomes,
            onDismiss = { showDialog = false },
            onSave = vm::saveIncome,
            onDelete = vm::deleteIncome,
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
private fun IncomeDialog(
    incomes: List<IncomeEntity>,
    onDismiss: () -> Unit,
    onSave: (IncomeEntity) -> Unit,
    onDelete: (IncomeEntity) -> Unit,
) {
    val kinds = listOf("Salário", "VR", "VM", "Outra renda")
    var kind by remember { mutableStateOf("Salário") }
    var amount by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renda e benefícios") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cadastre salário e outras rendas em dinheiro. Informe o saldo mensal de VR/VM para acompanhar compras; benefícios não entram na sobra disponível para contas.", style = MaterialTheme.typography.bodySmall)
                incomes.forEach { income ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            kind = income.description
                            amount = Money.formatPlain(income.amountCents)
                            selectedId = income.id
                            error = false
                        }, modifier = Modifier.weight(1f)) {
                            Text("${income.description}: ${Money.format(income.amountCents)}")
                        }
                        TextButton(onClick = { onDelete(income); if (selectedId == income.id) { selectedId = null; amount = "" } }) { Text("Remover") }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    kinds.forEach { label ->
                        androidx.compose.material3.FilterChip(selected = kind == label, onClick = {
                            kind = label; selectedId = incomes.firstOrNull { it.description == label }?.id
                            amount = incomes.firstOrNull { it.description == label }?.let { Money.formatPlain(it.amountCents) } ?: ""
                        }, label = { Text(label) })
                    }
                }
                MoneyField(amount, { amount = it; error = false }, "Valor mensal (R$)")
                if (error) Text("Digite um valor válido, por exemplo 3.500,00.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(amount)
                if (cents == null || cents <= 0) error = true
                else {
                    val existing = incomes.firstOrNull { it.description == kind }
                    onSave(IncomeEntity(id = selectedId ?: existing?.id ?: 0L, description = kind, amountCents = cents))
                    selectedId = null; amount = ""
                }
            }) { Text("Adicionar/atualizar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Concluir") } },
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
            StepRow(hasSalary, "Informe renda e benefícios", "Informar", onSalary)
            StepRow(hasBills, "Cadastre suas contas (aluguel, luz, mercado...)", "Cadastrar", onBills)
            StepRow(hasInstallments, "Registre compras, à vista ou parceladas", "Cadastrar", onInstallments)
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
