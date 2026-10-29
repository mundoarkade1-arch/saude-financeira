package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.domain.BillStats
import br.com.saudefinanceira.domain.BillStatsCalc
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
    var tab by remember { mutableIntStateOf(0) }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BillEntity?>(null) }
    var deleting by remember { mutableStateOf<BillEntity?>(null) }

    val now = YearMonth.now()
    val byBill = entries.groupBy { it.billId }
    val type = if (tab == 0) BillType.FIXED else BillType.VARIABLE
    val list = bills.filter { it.type == type }
    val total = list.sumOf { it.amountCents }

    Box(Modifier.fillMaxSize().padding(padding)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = if (tab == 0) "Contas fixas" else "Contas variáveis",
                value = Money.format(total),
                caption = if (tab == 0) "Todo mês, por tempo indeterminado"
                else "Atualize o valor de cada conta a cada mês",
            )
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Fixas") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Variáveis") })
            }
            if (list.isEmpty()) {
                EmptyState(
                    title = if (tab == 0) "Nenhuma conta fixa" else "Nenhuma conta variável",
                    text = if (tab == 0) "Toque em + para adicionar aluguel, internet, assinaturas..."
                    else "Toque em + para adicionar luz, água, gasolina, mercado...",
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
                        BillRow(bill, stats, onClick = { editing = bill }, onDelete = { deleting = bill })
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
}

@Composable
private fun BillRow(bill: BillEntity, stats: BillStats?, onClick: () -> Unit, onDelete: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(bill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    bill.dueDay?.let { "Vence dia $it" } ?: "Sem vencimento definido",
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
            Text(Money.format(bill.amountCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir") }
        }
    }
}

@Composable
private fun BillDialog(bill: BillEntity?, type: BillType, onDismiss: () -> Unit, onSave: (BillEntity) -> Unit) {
    var name by remember { mutableStateOf(bill?.name ?: "") }
    var amount by remember { mutableStateOf(bill?.let { Money.formatPlain(it.amountCents) } ?: "") }
    var day by remember { mutableStateOf(bill?.dueDay?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bill == null) "Nova conta" else "Editar conta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome (ex.: Aluguel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MoneyField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = if (type == BillType.FIXED) "Valor mensal (R$)" else "Valor deste mês (R$)",
                )
                NumberField(day, { day = it; error = null }, "Dia de vencimento (opcional)")
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
                    onSave(base.copy(name = name.trim(), amountCents = cents, dueDay = if (day.isBlank()) null else d))
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
