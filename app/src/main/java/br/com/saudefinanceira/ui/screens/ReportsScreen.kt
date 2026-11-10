package br.com.saudefinanceira.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.data.MonthSnapshotEntity
import br.com.saudefinanceira.domain.HistoryCsv
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.EmptyState
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.util.Money
import java.time.YearMonth

@Composable
fun ReportsScreen(vm: FinanceViewModel, padding: PaddingValues) {
    val history by vm.history.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val months = remember(history) {
        history.map { YearMonth.of(it.year, it.month) }.distinct().sorted()
    }
    var from by remember(months) { mutableStateOf(months.firstOrNull() ?: YearMonth.now()) }
    var through by remember(months) { mutableStateOf(months.lastOrNull() ?: YearMonth.now()) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val selected = history.filter {
        val month = YearMonth.of(it.year, it.month)
        month >= from && month <= through
    }.sortedWith(compareByDescending<MonthSnapshotEntity> { it.year }.thenByDescending { it.month })
    val totals = selected.fold(ReportTotals()) { acc, item ->
        acc.copy(
            income = acc.income + item.incomeCents,
            expenses = acc.expenses + item.fixedCents + item.variableCents + item.installmentsCents + item.debtPaymentsCents,
        )
    }

    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri != null) {
            exportMessage = try {
                val csv = HistoryCsv.export(history, from, through)
                val stream = context.contentResolver.openOutputStream(uri) ?: error("Não foi possível abrir o arquivo.")
                stream.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                "Relatório CSV salvo com sucesso."
            } catch (_: Exception) {
                "Não foi possível salvar o relatório. Tente novamente."
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding)) {
        ScreenHeader(title = "Relatórios", caption = "Compare sua renda, gastos e sobra ao longo do tempo.")
        if (history.isEmpty()) {
            EmptyState("Ainda sem histórico", "Use o app por alguns meses para montar seu primeiro relatório.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Período", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MonthSelector("De", from, months) { picked -> from = picked; if (through < picked) through = picked }
                                MonthSelector("Até", through, months) { picked -> through = picked; if (from > picked) from = picked }
                            }
                            Text("${selected.size} ${if (selected.size == 1) "mês" else "meses"} no período")
                            Text("Renda: ${Money.format(totals.income)}")
                            Text("Gastos: ${Money.format(totals.expenses)}")
                            Text("Sobra: ${Money.format(totals.income - totals.expenses)}", fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { export.launch("saude-financeira-${from}-${through}.csv") },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Exportar período em CSV") }
                            exportMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
                items(selected, key = { "${it.year}-${it.month}" }) { item ->
                    val expenses = item.fixedCents + item.variableCents + item.installmentsCents + item.debtPaymentsCents
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("%02d/%d".format(item.month, item.year), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Renda  ${Money.format(item.incomeCents)}")
                            Text("Fixas ${Money.format(item.fixedCents)}  ·  Variáveis ${Money.format(item.variableCents)}")
                            Text("Parcelas ${Money.format(item.installmentsCents)}  ·  Acordos ${Money.format(item.debtPaymentsCents)}")
                            Text("Gastos ${Money.format(expenses)}")
                            Text("Sobra  ${Money.format(item.incomeCents - expenses)}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private data class ReportTotals(val income: Long = 0L, val expenses: Long = 0L)

@Composable
private fun MonthSelector(label: String, value: YearMonth, options: List<YearMonth>, onSelect: (YearMonth) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { expanded = true }) { Text("%02d/%d".format(value.monthValue, value.year)) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("%02d/%d".format(option.monthValue, option.year)) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}
