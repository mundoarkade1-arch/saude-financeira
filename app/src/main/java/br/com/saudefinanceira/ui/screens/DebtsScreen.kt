package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import br.com.saudefinanceira.data.DebtEntity
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.EmptyState
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.NumberField
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.util.Money

@Composable
fun DebtsScreen(vm: FinanceViewModel, padding: PaddingValues, showHeader: Boolean = true) {
    val debts by vm.debts.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<DebtEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<DebtEntity?>(null) }
    val total = debts.sumOf { it.balanceCents }
    Box(Modifier.fillMaxSize().padding(padding)) {
      Column(Modifier.fillMaxSize()) {
        if (showHeader) ScreenHeader("Dívidas", value = Money.format(total), caption = "Saldo informado por você; o app não consulta birôs de crédito.")
        if (debts.isEmpty()) {
            EmptyState("Nenhuma dívida cadastrada", "Registre credor, saldo, juros e se seu nome está negativado para acompanhar uma negociação.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Como priorizar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Mantenha as contas essenciais em dia. Entre as dívidas, compare juros e custo total; cartão rotativo e cheque especial costumam exigir atenção rápida. Peça ao credor o saldo para quitação e propostas por escrito, compare o total final e só aceite parcela que caiba no orçamento.")
                        }
                    }
                }
                items(debts, key = { it.id }) { debt ->
                    AppCard(Modifier.fillMaxWidth(), onClick = { editing = debt }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(debt.creditor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(debt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { deleting = debt }) { Icon(Icons.Filled.Delete, contentDescription = "Excluir dívida") }
                            }
                            Text(Money.format(debt.balanceCents), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            val monthlyRate = "%.2f".format(debt.monthlyInterestBasisPoints / 100.0)
                            Text("Juros informados: $monthlyRate% ao mês${debt.dueDay?.let { " · vence dia $it" } ?: ""}")
                            if (debt.plannedPaymentCents > 0) Text("Parcela planejada: ${Money.format(debt.plannedPaymentCents)} por mês")
                            if (debt.isNegative) Text("Nome negativado: sim", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            else Text("Nome negativado: não", color = MaterialTheme.colorScheme.primary)
                            Text("Toque para atualizar saldo ou dados da negociação.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
      }
      FloatingActionButton(onClick = { creating = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), content = { Icon(Icons.Filled.Add, "Adicionar dívida") })
    }
    if (creating) DebtDialog(null, onDismiss = { creating = false }, onSave = { vm.saveDebt(it); creating = false })
    editing?.let { debt -> DebtDialog(debt, onDismiss = { editing = null }, onSave = { vm.saveDebt(it); editing = null }) }
    deleting?.let { debt -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Excluir dívida?") }, text = { Text("${debt.creditor} será removida do seu acompanhamento.") }, confirmButton = { TextButton(onClick = { vm.deleteDebt(debt); deleting = null }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } }) }
}

@Composable
private fun DebtDialog(debt: DebtEntity?, onDismiss: () -> Unit, onSave: (DebtEntity) -> Unit) {
    var creditor by remember(debt?.id) { mutableStateOf(debt?.creditor.orEmpty()) }
    var description by remember(debt?.id) { mutableStateOf(debt?.description.orEmpty()) }
    var balance by remember(debt?.id) { mutableStateOf(debt?.let { Money.formatPlain(it.balanceCents) }.orEmpty()) }
    var rate by remember(debt?.id) { mutableStateOf(debt?.let { "%.2f".format(it.monthlyInterestBasisPoints / 100.0) }.orEmpty()) }
    var dueDay by remember(debt?.id) { mutableStateOf(debt?.dueDay?.toString().orEmpty()) }
    var payment by remember(debt?.id) { mutableStateOf(debt?.let { Money.formatPlain(it.plannedPaymentCents) }.orEmpty()) }
    var negative by remember(debt?.id) { mutableStateOf(debt?.isNegative ?: false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (debt == null) "Adicionar dívida" else "Atualizar dívida") }, text = {
        Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(creditor, { creditor = it; error = null }, label = { Text("Credor (banco, loja, pessoa)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it; error = null }, label = { Text("Tipo (cartão, empréstimo...)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            MoneyField(balance, { balance = it; error = null }, "Saldo atual da dívida (R$)")
            OutlinedTextField(rate, { rate = it; error = null }, label = { Text("Juros ao mês (%) se souber") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            NumberField(dueDay, { dueDay = it; error = null }, "Dia de vencimento (opcional)")
            MoneyField(payment, { payment = it; error = null }, "Parcela negociada (opcional)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(negative, { negative = it })
                Text("Meu nome está negativado por esta dívida")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = {
        val cents = Money.parse(balance)
        val paymentCents = if (payment.isBlank()) 0L else Money.parse(payment)
        val rateBasisLong = if (rate.isBlank()) 0L else Money.parse(rate)
        val day = dueDay.toIntOrNull()
        when {
            creditor.isBlank() -> error = "Informe quem é o credor."
            description.isBlank() -> error = "Informe o tipo de dívida."
            cents == null || cents <= 0L -> error = "Informe o saldo atual."
            rateBasisLong == null || rateBasisLong !in 0L..100_000L -> error = "Confira a taxa de juros mensal."
            dueDay.isNotBlank() && (day == null || day !in 1..31) -> error = "O vencimento deve ser de 1 a 31."
            paymentCents == null || paymentCents < 0L -> error = "Confira o valor da parcela."
            else -> onSave((debt ?: DebtEntity(creditor = "", description = "", balanceCents = 0L)).copy(
                creditor = creditor.trim(), description = description.trim(), balanceCents = cents,
                monthlyInterestBasisPoints = rateBasisLong.toInt(), dueDay = if (dueDay.isBlank()) null else day,
                isNegative = negative, plannedPaymentCents = paymentCents,
            ))
        }
    }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
