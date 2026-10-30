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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.data.InstallmentEntity
import br.com.saudefinanceira.domain.CardInvoice
import br.com.saudefinanceira.domain.CardInvoices
import br.com.saudefinanceira.domain.InstallmentCalc
import br.com.saudefinanceira.domain.InstallmentStatus
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.EmptyState
import br.com.saudefinanceira.ui.components.GradientProgress
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.NumberField
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.util.Money
import java.time.YearMonth

@Composable
fun InstallmentsScreen(vm: FinanceViewModel, padding: PaddingValues) {
    val items by vm.installments.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InstallmentEntity?>(null) }
    var deleting by remember { mutableStateOf<InstallmentEntity?>(null) }

    val now = YearMonth.now()
    val rows = items.map { it to InstallmentCalc.status(it, now) }
    val monthly = rows.sumOf { it.second.monthlyCents }
    val remaining = rows.sumOf { it.second.remainingCents }
    val invoices = CardInvoices.compute(items, now)

    Box(Modifier.fillMaxSize().padding(padding)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Parcelas do mês",
                value = Money.format(monthly),
                caption = "Ainda falta pagar ${Money.format(remaining)} no total",
            )
            if (rows.isEmpty()) {
                EmptyState(
                    title = "Nenhuma compra parcelada",
                    text = "Toque em + para adicionar, por exemplo, um micro-ondas em 12 vezes ou uma compra no cartão em 10 vezes.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (invoices.isNotEmpty()) {
                        item { InvoicesCard(invoices, now) }
                    }
                    items(rows, key = { it.first.id }) { (item, st) ->
                        InstallmentCard(item, st, onClick = { editing = item }, onDelete = { deleting = item })
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = "Adicionar compra parcelada") }
    }

    if (creating) {
        InstallmentDialog(null, onDismiss = { creating = false }, onSave = { vm.saveInstallment(it); creating = false })
    }
    editing?.let { item ->
        InstallmentDialog(item, onDismiss = { editing = null }, onSave = { vm.saveInstallment(it); editing = null })
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir compra?") },
            text = { Text("\"${item.description}\" será removida. Isso não pode ser desfeito.") },
            confirmButton = { TextButton(onClick = { vm.deleteInstallment(item); deleting = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun InstallmentCard(item: InstallmentEntity, st: InstallmentStatus, onClick: () -> Unit, onDelete: () -> Unit) {
    val first = YearMonth.of(item.firstYear, item.firstMonth)
    val last = first.plusMonths((item.installments - 1).toLong())
    val fraction = if (st.notStarted) 0f else st.current.toFloat() / st.total
    val label = when {
        st.notStarted -> "Começa em ${monthLabel(first)}"
        st.finished -> "Quitada"
        else -> "Parcela ${st.current}/${st.total}"
    }
    val parcel = if (st.monthlyCents > 0) st.monthlyCents
    else InstallmentCalc.parcelAmount(item.totalCents, item.installments, 1)

    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val card = item.card
                    if (!card.isNullOrBlank()) {
                        Text(card, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Excluir") }
            }
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            GradientProgress(fraction)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${Money.format(parcel)} por mês", style = MaterialTheme.typography.bodyMedium)
                Text("Faltam ${Money.format(st.remainingCents)}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Termina em ${monthLabel(last)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstallmentDialog(item: InstallmentEntity?, onDismiss: () -> Unit, onSave: (InstallmentEntity) -> Unit) {
    val now = YearMonth.now()
    val st = item?.let { InstallmentCalc.status(it, now) }
    // Compras que ainda não começaram ou já terminaram mantêm o mês de início original.
    val lockStart = st != null && (st.notStarted || st.finished)

    var description by remember { mutableStateOf(item?.description ?: "") }
    var byParcel by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf(item?.let { Money.formatPlain(it.totalCents) } ?: "") }
    var parcels by remember { mutableStateOf(item?.installments?.toString() ?: "") }
    var current by remember { mutableStateOf(st?.current?.coerceAtLeast(1)?.toString() ?: "1") }
    var card by remember { mutableStateOf(item?.card ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Nova compra parcelada" else "Editar compra parcelada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; error = null },
                    label = { Text("O que foi comprado (ex.: Micro-ondas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !byParcel, onClick = { byParcel = false }, label = { Text("Valor total") })
                    FilterChip(selected = byParcel, onClick = { byParcel = true }, label = { Text("Valor da parcela") })
                }
                MoneyField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = if (byParcel) "Valor de cada parcela (R$)" else "Valor total (R$)",
                )
                NumberField(parcels, { parcels = it; error = null }, "Número de parcelas")
                if (!lockStart) {
                    NumberField(current, { current = it; error = null }, "Em qual parcela você está agora?")
                }
                OutlinedTextField(
                    value = card,
                    onValueChange = { card = it },
                    label = { Text("Cartão (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(amount)
                val n = parcels.toIntOrNull()
                val cur = if (lockStart) 1 else current.toIntOrNull()
                if (description.isBlank()) {
                    error = "Informe o que foi comprado."
                } else if (cents == null || cents <= 0L) {
                    error = "Informe um valor válido."
                } else if (n == null || n < 1 || n > 120) {
                    error = "O número de parcelas deve ser de 1 a 120."
                } else if (cur == null || cur < 1 || cur > n) {
                    error = "A parcela atual deve estar entre 1 e $n."
                } else {
                    val first = if (lockStart && item != null) YearMonth.of(item.firstYear, item.firstMonth)
                    else now.minusMonths((cur - 1).toLong())
                    val base = item ?: InstallmentEntity(
                        description = "", totalCents = 0L, installments = 1, firstYear = first.year, firstMonth = first.monthValue,
                    )
                    onSave(
                        base.copy(
                            description = description.trim(),
                            totalCents = if (byParcel) cents * n else cents,
                            installments = n,
                            firstYear = first.year,
                            firstMonth = first.monthValue,
                            card = card.trim().ifBlank { null },
                        )
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun InvoicesCard(invoices: List<CardInvoice>, now: YearMonth) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Faturas por cartão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            invoices.forEach { inv ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(inv.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Este mês: ${Money.format(inv.months.first())}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        inv.months.drop(1).mapIndexed { i, v ->
                            "${monthLabel(now.plusMonths((i + 1).toLong()))}: ${Money.format(v)}"
                        }.joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Última parcela em ${monthLabel(inv.lastMonth)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                "Só as parcelas cadastradas entram aqui; compras à vista no cartão não são contadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
