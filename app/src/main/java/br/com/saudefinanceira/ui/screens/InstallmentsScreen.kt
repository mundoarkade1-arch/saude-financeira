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
    var deleting by remember { mutableStateOf<InstallmentEntity?>(null) }

    val now = YearMonth.now()
    val rows = items.map { it to InstallmentCalc.status(it, now) }
    val monthly = rows.sumOf { it.second.monthlyCents }
    val remaining = rows.sumOf { it.second.remainingCents }

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
                    items(rows, key = { it.first.id }) { (item, st) ->
                        InstallmentCard(item, st, onDelete = { deleting = item })
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
        InstallmentDialog(onDismiss = { creating = false }, onSave = { vm.saveInstallment(it); creating = false })
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
private fun InstallmentCard(item: InstallmentEntity, st: InstallmentStatus, onDelete: () -> Unit) {
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

    AppCard(Modifier.fillMaxWidth()) {
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
private fun InstallmentDialog(onDismiss: () -> Unit, onSave: (InstallmentEntity) -> Unit) {
    var description by remember { mutableStateOf("") }
    var byParcel by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var parcels by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("1") }
    var card by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova compra parcelada") },
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
                NumberField(current, { current = it; error = null }, "Em qual parcela você está agora?")
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
                val cur = current.toIntOrNull()
                if (description.isBlank()) {
                    error = "Informe o que foi comprado."
                } else if (cents == null || cents <= 0L) {
                    error = "Informe um valor válido."
                } else if (n == null || n < 1 || n > 120) {
                    error = "O número de parcelas deve ser de 1 a 120."
                } else if (cur == null || cur < 1 || cur > n) {
                    error = "A parcela atual deve estar entre 1 e $n."
                } else {
                    val first = YearMonth.now().minusMonths((cur - 1).toLong())
                    onSave(
                        InstallmentEntity(
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
