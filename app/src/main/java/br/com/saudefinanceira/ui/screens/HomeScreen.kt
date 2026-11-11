package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.data.IncomeEntity
import br.com.saudefinanceira.data.InstallmentEntity
import br.com.saudefinanceira.data.PlanPrefs
import br.com.saudefinanceira.domain.DailyBudgetCalc
import br.com.saudefinanceira.domain.InstallmentCalc
import br.com.saudefinanceira.domain.PayCycle
import br.com.saudefinanceira.domain.SalarySplitCalc
import br.com.saudefinanceira.domain.SpendingBreakdown
import br.com.saudefinanceira.domain.TipEngine
import br.com.saudefinanceira.domain.TipLevel
import br.com.saudefinanceira.domain.UpcomingBills
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.HeaderProgress
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.NumberField
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.ui.theme.BrandGradient
import br.com.saudefinanceira.update.UpdateInfo
import br.com.saudefinanceira.util.Money
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ptBR = Locale("pt", "BR")
private val dayMonth = DateTimeFormatter.ofPattern("dd/MM")

/** Tela principal: quanto posso gastar hoje, botão "Gastei" e as contas dos próximos dias. */
@Composable
fun HomeScreen(
    vm: FinanceViewModel,
    padding: PaddingValues,
    pendingUpdate: UpdateInfo?,
    onOpenSettings: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenSummary: () -> Unit,
) {
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val incomes by vm.incomes.collectAsStateWithLifecycle()
    val bills by vm.bills.collectAsStateWithLifecycle()
    val quick by vm.quickExpenses.collectAsStateWithLifecycle()
    var showIncome by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val planPrefs = remember(context) { PlanPrefs(context) }
    val reserveCents = planPrefs.reserveCents

    val today = LocalDate.now()
    val hasIncome = s.incomeCents > 0L
    val budget = DailyBudgetCalc.compute(s, reserveCents, quick, today, PayCycle.mainPayDay(incomes))
    val todayExpenses = quick.filter { it.year == today.year && it.month == today.monthValue && it.day == today.dayOfMonth }
    val upcoming = UpcomingBills.next(bills, today)

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        if (!hasIncome) {
            ScreenHeader(
                title = "Vamos começar",
                caption = "Diga quanto você ganha e o app mostra quanto você pode gastar por dia.",
            )
        } else {
            ScreenHeader(
                title = "Hoje você pode gastar",
                value = Money.format(budget.leftTodayCents.coerceAtLeast(0L)),
                caption = when {
                    budget.freeCents <= 0L -> "Suas contas já passam do que você ganha. Veja na aba Contas o que pagar primeiro."
                    budget.leftTodayCents < 0L -> "Hoje já passou do limite. Tente não gastar mais."
                    else -> "Dá ${Money.format(budget.todayBudgetCents)} por dia até ${budget.cycle.end.format(dayMonth)} (${budget.daysLeft} ${if (budget.daysLeft == 1) "dia" else "dias"})"
                },
            )
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (pendingUpdate != null) {
                AppCard(Modifier.fillMaxWidth(), onClick = onOpenSettings) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tem uma versão nova do app", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Toque aqui para atualizar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!hasIncome) {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Quanto você ganha por mês?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = { showIncome = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Informar o que eu ganho") }
                        Text("2. Depois, cadastre suas contas (aluguel, luz, água...)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onOpenBills) { Text("Ir para as contas") }
                    }
                }
            } else {
                SpentTodayCard(
                    todayExpenses = todayExpenses,
                    onAdd = vm::addQuickExpense,
                    onDelete = vm::deleteQuickExpense,
                )

                AppCard(Modifier.fillMaxWidth(), onClick = onOpenBills) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Contas que vencem em breve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (upcoming.isEmpty()) {
                            Text(
                                if (bills.isEmpty()) "Você ainda não cadastrou contas. Toque aqui para começar."
                                else "Nenhuma conta vence nos próximos 7 dias.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            upcoming.take(5).forEach { u ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(u.bill.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            when (u.daysUntil) {
                                                0 -> "vence hoje"
                                                1 -> "vence amanhã"
                                                else -> "vence em ${u.daysUntil} dias"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (u.daysUntil <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(Money.format(u.bill.amountCents), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text(
                                "Total: ${Money.format(upcoming.sumOf { it.bill.amountCents })}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { showIncome = true }) { Text("Mudar o que eu ganho") }
                    TextButton(onClick = onOpenSummary) { Text("Ver resumo do mês") }
                }
            }
        }
    }

    if (showIncome) {
        IncomeDialog(
            incomes = incomes,
            onDismiss = { showIncome = false },
            onSave = vm::saveIncome,
            onDelete = vm::deleteIncome,
        )
    }
}

/** Resumo do mês: tudo o que antes ficava na tela inicial, agora fora do caminho. */
@Composable
fun SummaryScreen(
    vm: FinanceViewModel,
    padding: PaddingValues,
    onOpenTips: () -> Unit,
    onOpenReports: () -> Unit,
) {
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val debts by vm.debts.collectAsStateWithLifecycle()
    val installments by vm.installments.collectAsStateWithLifecycle()
    val bills by vm.bills.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val rising by vm.risingBills.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val planPrefs = remember(context) { PlanPrefs(context) }
    var reserveCents by remember { mutableStateOf(planPrefs.reserveCents) }
    var showReserve by remember { mutableStateOf(false) }

    val now = YearMonth.now()
    val monthName = now.month.getDisplayName(TextStyle.FULL, ptBR)
    val committed = if (s.incomeCents > 0) s.spentCents.toFloat() / s.incomeCents else 0f
    val tips = TipEngine.generate(s, rising)
    val highlight = tips.firstOrNull { it.level != TipLevel.INFO } ?: tips.first()

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = "Quanto sobra em $monthName",
            value = Money.format(s.balanceCents),
            caption = if (s.incomeCents == 0L) "Informe quanto você ganha na tela Hoje"
            else "${(committed * 100).toInt()}% do que você ganha já tem destino",
        ) {
            if (s.incomeCents > 0) {
                Spacer(Modifier.height(12.dp))
                HeaderProgress(committed)
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (s.incomeCents > 0L) {
                SalaryPlanCard(
                    split = SalarySplitCalc.compute(bills, s, reserveCents),
                    incomeCents = s.incomeCents,
                    reserveCents = reserveCents,
                    onEditReserve = { showReserve = true },
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Contas fixas", Money.format(s.fixedCents), Modifier.weight(1f))
                StatCard("Contas que mudam", Money.format(s.variableCents), Modifier.weight(1f))
                StatCard("Parcelas", Money.format(s.installmentsCents), Modifier.weight(1f))
            }

            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(highlight.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(highlight.message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onOpenTips) { Text("Ver mais dicas") }
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
                        Text("Relatórios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Compare os meses e salve numa planilha.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            ProjectionCard(installments, now)
        }
    }

    if (showReserve) {
        ReserveDialog(
            currentCents = reserveCents,
            onDismiss = { showReserve = false },
            onSave = { planPrefs.reserveCents = it; reserveCents = it; showReserve = false },
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
            Text("Parcelas que vêm por aí (R$)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    var payDay by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var dayError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quanto você ganha por mês") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Informe o que entra por mês (salário, bico, outra renda). VR e VM (vale) ficam separados e não entram na conta do dia.", style = MaterialTheme.typography.bodyMedium)
                incomes.forEach { income ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            kind = income.description
                            amount = Money.formatPlain(income.amountCents)
                            payDay = income.payDay?.toString() ?: ""
                            selectedId = income.id
                            error = false
                            dayError = false
                        }, modifier = Modifier.weight(1f)) {
                            Text("${income.description}: ${Money.format(income.amountCents)}" + (income.payDay?.let { " · dia $it" } ?: ""))
                        }
                        TextButton(onClick = { onDelete(income); if (selectedId == income.id) { selectedId = null; amount = ""; payDay = "" } }) { Text("Remover") }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    kinds.forEach { label ->
                        androidx.compose.material3.FilterChip(selected = kind == label, onClick = {
                            kind = label; selectedId = incomes.firstOrNull { it.description == label }?.id
                            amount = incomes.firstOrNull { it.description == label }?.let { Money.formatPlain(it.amountCents) } ?: ""
                            payDay = incomes.firstOrNull { it.description == label }?.payDay?.toString() ?: ""
                            dayError = false
                        }, label = { Text(label) })
                    }
                }
                MoneyField(amount, { amount = it; error = false }, "Valor mensal (R$)")
                NumberField(payDay, { payDay = it; dayError = false }, "Dia em que o dinheiro cai (se souber)")
                Text("Com o dia, o app mostra quanto dá para gastar por dia até o próximo pagamento.", style = MaterialTheme.typography.bodySmall)
                if (dayError) Text("O dia deve ser de 1 a 31.", color = MaterialTheme.colorScheme.error)
                if (error) Text("Digite um valor válido, por exemplo 3.500,00.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(amount)
                val day = payDay.trim().toIntOrNull()
                if (cents == null || cents <= 0) error = true
                else if (payDay.isNotBlank() && (day == null || day !in 1..31)) dayError = true
                else {
                    val existing = incomes.firstOrNull { it.description == kind }
                    onSave(IncomeEntity(id = selectedId ?: existing?.id ?: 0L, description = kind, amountCents = cents, payDay = if (payDay.isBlank()) null else day))
                    selectedId = null; amount = ""; payDay = ""
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Concluir") } },
    )
}
