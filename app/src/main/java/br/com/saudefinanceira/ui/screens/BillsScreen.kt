package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillEntryEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.domain.BillStats
import br.com.saudefinanceira.domain.BillStatsCalc
import br.com.saudefinanceira.domain.PaymentPlan
import br.com.saudefinanceira.domain.Priority
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.EmptyState
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.NumberField
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.util.Money
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun BillsScreen(vm: FinanceViewModel, padding: PaddingValues) {
    val bills by vm.bills.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val snap by vm.snapshot.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BillEntity?>(null) }
    var deleting by remember { mutableStateOf<BillEntity?>(null) }
    var historyBill by remember { mutableStateOf<BillEntity?>(null) }

    val now = YearMonth.now()
    val byBill = entries.groupBy { it.billId }
    // Abas: 0 = Ordem de pagamento (inicial), 1 = Fixas, 2 = Variáveis.
    val type = if (tab == 2) BillType.VARIABLE else BillType.FIXED
    val list = bills.filter { it.type == type }
    val total = list.sumOf { it.amountCents }
    // Renda em dinheiro que sobra para as contas, depois de descontar parcelas e acordos de dívidas.
    val availableForBills = snap.incomeCents - snap.installmentsCents - snap.debtPaymentsCents

    Box(Modifier.fillMaxSize().padding(padding)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = when (tab) {
                    0 -> "O que pagar primeiro"
                    1 -> "Contas que não mudam"
                    else -> "Contas que mudam todo mês"
                },
                value = Money.format(if (tab == 0) availableForBills.coerceAtLeast(0L) else total),
                caption = when (tab) {
                    0 -> "É o que sobra do seu dinheiro para pagar as contas"
                    1 -> "Mesmo valor todo mês, como aluguel e internet"
                    else -> "Luz, água, gás... Mude o valor quando a conta chegar"
                },
            )
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Ordem") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Fixas") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Que mudam") })
            }
            if (tab == 0) {
                PaymentOrderList(bills, availableForBills, snap.incomeCents > 0L, onEdit = { editing = it })
            } else if (list.isEmpty()) {
                EmptyState(
                    title = if (tab == 1) "Nenhuma conta fixa" else "Nenhuma conta que muda",
                    text = if (tab == 1) "Toque em + para adicionar aluguel, internet, assinaturas..."
                    else "Toque em + para adicionar luz, água, gás, mercado...",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(list, key = { it.id }) { bill ->
                        val stats = if (bill.type == BillType.VARIABLE) {
                            BillStatsCalc.compute(byBill[bill.id].orEmpty(), now)
                        } else null
                        BillRow(
                            bill, stats,
                            onClick = { editing = bill },
                            onDelete = { deleting = bill },
                            onHistory = { historyBill = bill },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = "Adicionar conta") }
    }

    if (creating) {
        BillDialog(null, type, onDismiss = { creating = false }, onSave = { vm.saveBill(it); creating = false })
    }
    editing?.let { b ->
        BillDialog(b, b.type, onDismiss = { editing = null }, onSave = { vm.saveBill(it); editing = null })
    }
    deleting?.let { b ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir conta?") },
            text = { Text("\"${b.name}\" e o histórico dela serão removidos. Isso não pode ser desfeito.") },
            confirmButton = { TextButton(onClick = { vm.deleteBill(b); deleting = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }
    historyBill?.let { bill ->
        BillHistoryDialog(
            bill = bill,
            entries = byBill[bill.id].orEmpty(),
            onDismiss = { historyBill = null },
            onSave = vm::saveBillEntry,
            onDelete = vm::deleteBillEntry,
        )
    }
}

@Composable
private fun PaymentOrderList(
    bills: List<BillEntity>,
    availableCents: Long,
    hasIncome: Boolean,
    onEdit: (BillEntity) -> Unit,
) {
    if (bills.isEmpty()) {
        EmptyState(
            title = "Nenhuma conta cadastrada",
            text = "Toque em + para cadastrar sua primeira conta, como aluguel ou luz.",
        )
        return
    }
    val plan = PaymentPlan.build(bills, availableCents)
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    when {
                        !hasIncome -> "Diga quanto você ganha na tela Hoje para ver até onde o dinheiro chega."
                        plan.uncoveredCount == 0 -> "Seu dinheiro cobre todas as contas e ainda sobram ${Money.format(plan.leftoverCents)}."
                        else -> "Seu dinheiro cobre ${plan.coveredCount} de ${plan.steps.size} contas. Pague de cima para baixo. Nas últimas, fale com quem cobra antes de vencer."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "As contas de cima são as que você não pode deixar de pagar (moradia, luz, água, comida). Toque numa conta para mudar o lugar dela.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(plan.steps.size) { index ->
            val step = plan.steps[index]
            AppCard(Modifier.fillMaxWidth(), onClick = { onEdit(step.bill) }) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("${index + 1}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(step.bill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            Priority.label(step.bill.priority) + (step.bill.dueDay?.let { " · vence dia $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (hasIncome) {
                            Text(
                                if (step.covered) "✓ Tem dinheiro para pagar" else "Faltam ${Money.format(step.missingCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (step.covered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Text(Money.format(step.bill.amountCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BillRow(bill: BillEntity, stats: BillStats?, onClick: () -> Unit, onDelete: () -> Unit, onHistory: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(bill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    Priority.label(bill.priority) + " · " + (bill.dueDay?.let { "vence dia $it" } ?: "sem vencimento definido"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (stats != null) {
                    if (stats.months >= 2) {
                        Text(
                            "Média (${stats.months} meses): ${Money.format(stats.averageCents)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val trend = stats.trendPercent
                    if (trend != null && abs(trend) >= 1) {
                        val up = trend > 0
                        Text(
                            (if (up) "↑ $trend% acima" else "↓ ${abs(trend)}% abaixo") + " dos meses anteriores",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (up) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                    }
                    val stale = stats.staleSince
                    if (stale != null) {
                        Text(
                            "Valor de ${monthLabel(stale)}. Toque para atualizar este mês.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Money.format(bill.amountCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (bill.type == BillType.VARIABLE) {
                    TextButton(onClick = onHistory) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Histórico")
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir") }
        }
    }
}

@Composable
private fun BillHistoryDialog(
    bill: BillEntity,
    entries: List<BillEntryEntity>,
    onDismiss: () -> Unit,
    onSave: (BillEntryEntity) -> Unit,
    onDelete: (BillEntryEntity) -> Unit,
) {
    var editing by remember(bill.id) { mutableStateOf<BillEntryEntity?>(null) }
    var adding by remember(bill.id) { mutableStateOf(false) }
    var removing by remember { mutableStateOf<BillEntryEntity?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Histórico • ${bill.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Toque em um mês para corrigir o valor. O histórico começa quando o app passou a registrar os lançamentos.")
                if (entries.isEmpty()) Text("Ainda não há meses registrados.")
                LazyColumn(Modifier.height(280.dp)) {
                    items(entries.sortedWith(compareByDescending<BillEntryEntity> { it.year }.thenByDescending { it.month }), key = { it.id }) { entry ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { editing = entry }) {
                                Text("${monthLabel(YearMonth.of(entry.year, entry.month))}  ·  ${Money.format(entry.amountCents)}")
                            }
                            val current = entry.year == YearMonth.now().year && entry.month == YearMonth.now().monthValue
                            if (!current) IconButton(onClick = { removing = entry }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir lançamento")
                            }
                        }
                    }
                }
                HorizontalDivider()
                TextButton(onClick = { adding = true }) { Text("Adicionar lançamento de outro mês") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Concluir") } },
    )
    (editing ?: if (adding) BillEntryEntity(billId = bill.id, year = YearMonth.now().year, month = YearMonth.now().monthValue, amountCents = 0L) else null)?.let { selected ->
        BillEntryDialog(
            entry = selected,
            existingEntries = entries,
            onDismiss = { editing = null; adding = false },
            onSave = { onSave(it); editing = null; adding = false },
        )
    }
    removing?.let { entry ->
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text("Excluir lançamento?") },
            text = { Text("O valor de ${monthLabel(YearMonth.of(entry.year, entry.month))} será removido do histórico.") },
            confirmButton = { TextButton(onClick = { onDelete(entry); removing = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { removing = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun BillEntryDialog(
    entry: BillEntryEntity,
    existingEntries: List<BillEntryEntity>,
    onDismiss: () -> Unit,
    onSave: (BillEntryEntity) -> Unit,
) {
    var monthText by remember(entry.id, entry.year, entry.month) { mutableStateOf("%02d/%04d".format(entry.month, entry.year)) }
    var amountText by remember(entry.id, entry.amountCents) { mutableStateOf(if (entry.amountCents > 0L) Money.formatPlain(entry.amountCents) else "") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry.id == 0L) "Novo lançamento" else "Corrigir lançamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(monthText, { monthText = it; error = null }, label = { Text("Mês/ano (MM/AAAA)") }, singleLine = true)
                MoneyField(amountText, { amountText = it; error = null }, "Valor (R$)")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parts = monthText.trim().split('/')
                val month = parts.getOrNull(0)?.toIntOrNull()
                val year = parts.getOrNull(1)?.toIntOrNull()
                val amount = Money.parse(amountText)
                val selectedMonth = if (month != null && year != null && month in 1..12 && year in 1990..2200) YearMonth.of(year, month) else null
                if (parts.size != 2 || selectedMonth == null) error = "Informe um mês válido no formato MM/AAAA."
                else if (selectedMonth.isAfter(YearMonth.now())) error = "O histórico só pode conter o mês atual ou meses anteriores."
                else if (amount == null || amount !in 1L..10_000_000_000_000L) error = "Informe um valor válido de até R$ 100.000.000.000,00."
                else if (existingEntries.any { it.id != entry.id && it.year == year && it.month == month }) error = "Já existe um lançamento para esse mês."
                else onSave(entry.copy(year = selectedMonth.year, month = selectedMonth.monthValue, amountCents = amount))
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun BillDialog(bill: BillEntity?, type: BillType, onDismiss: () -> Unit, onSave: (BillEntity) -> Unit) {
    var name by remember { mutableStateOf(bill?.name ?: "") }
    var amount by remember { mutableStateOf(bill?.let { Money.formatPlain(it.amountCents) } ?: "") }
    var day by remember { mutableStateOf(bill?.dueDay?.toString() ?: "") }
    var priority by remember { mutableStateOf(bill?.priority ?: Priority.IMPORTANT) }
    var priorityTouched by remember { mutableStateOf(bill != null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bill == null) "Nova conta" else "Editar conta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                        if (!priorityTouched) priority = Priority.suggest(it)
                    },
                    label = { Text("Nome (ex.: Aluguel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MoneyField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = if (type == BillType.FIXED) "Valor mensal (R$)" else "Valor deste mês (R$)",
                )
                NumberField(day, { day = it; error = null }, "Dia que vence (se tiver)")
                Text("Se o dinheiro faltar, quando pagar esta conta?", style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(Priority.ESSENTIAL, Priority.IMPORTANT, Priority.FLEXIBLE).forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p; priorityTouched = true },
                            label = { Text(Priority.label(p)) },
                        )
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(amount)
                val d = day.trim().toIntOrNull()
                if (name.isBlank()) {
                    error = "Informe o nome da conta."
                } else if (cents == null || cents <= 0L) {
                    error = "Informe um valor válido."
                } else if (day.isNotBlank() && (d == null || d !in 1..31)) {
                    error = "O dia deve ser de 1 a 31."
                } else {
                    val base = bill ?: BillEntity(name = "", type = type, amountCents = 0L)
                    onSave(base.copy(name = name.trim(), amountCents = cents, dueDay = if (day.isBlank()) null else d, priority = priority))
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
