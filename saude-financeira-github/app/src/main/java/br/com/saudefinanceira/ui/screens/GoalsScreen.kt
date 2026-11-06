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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import br.com.saudefinanceira.data.GoalEntity
import br.com.saudefinanceira.domain.GoalCalc
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.GradientProgress
import br.com.saudefinanceira.ui.components.MoneyField
import br.com.saudefinanceira.ui.components.monthLabel
import br.com.saudefinanceira.util.Money
import java.time.YearMonth

@Composable
fun GoalsTab(vm: FinanceViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val now = YearMonth.now()
    val essentials = s.fixedCents + s.variableCents

    // creating != null -> diálogo de nova meta (nome e valor sugeridos)
    var creating by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var editing by remember { mutableStateOf<GoalEntity?>(null) }
    var deleting by remember { mutableStateOf<GoalEntity?>(null) }
    var amountDialog by remember { mutableStateOf<Pair<GoalEntity, Boolean>?>(null) } // true = guardar

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val hasEmergency = goals.any { it.name.equals(GoalCalc.EMERGENCY_NAME, ignoreCase = true) }
            if (!hasEmergency && essentials > 0L) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Comece pela reserva de emergência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Seis meses de gastos essenciais (contas fixas e variáveis) somam ${Money.format(essentials * 6)}. " +
                                    "Ela protege você de imprevistos sem precisar recorrer a dívidas.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = { creating = GoalCalc.EMERGENCY_NAME to essentials * 6 }) { Text("Criar essa meta") }
                        }
                    }
                }
            }
            if (goals.isEmpty() && (hasEmergency || essentials == 0L)) {
                item {
                    Text(
                        "Toque em + para criar uma meta, como a reserva de emergência, uma viagem ou a entrada de um bem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(goals, key = { it.id }) { goal ->
                val p = GoalCalc.progress(goal, s.balanceCents, now)
                AppCard(Modifier.fillMaxWidth(), onClick = { editing = goal }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(goal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${Money.format(goal.savedCents)} de ${Money.format(goal.targetCents)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            IconButton(onClick = { deleting = goal }) { Icon(Icons.Filled.Delete, contentDescription = "Excluir meta") }
                        }
                        GradientProgress(p.fraction)
                        Text(
                            if (p.done) "Meta atingida. Parabéns!" else "Faltam ${Money.format(p.remainingCents)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!p.done) {
                            val eta = p.eta
                            Text(
                                if (eta != null) "Guardando toda a sua sobra prevista (${Money.format(s.balanceCents)} por mês), você chega lá em ${monthLabel(eta)}."
                                else "Sem sobra prevista positiva neste mês, não dá para estimar o prazo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { amountDialog = goal to true }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                            OutlinedButton(onClick = { amountDialog = goal to false }, modifier = Modifier.weight(1f)) { Text("Retirar") }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { creating = "" to 0L },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Filled.Add, contentDescription = "Nova meta") }
    }

    creating?.let { (name, target) ->
        GoalDialog(null, name, target, onDismiss = { creating = null }, onSave = { vm.saveGoal(it); creating = null })
    }
    editing?.let { g ->
        GoalDialog(g, g.name, g.targetCents, onDismiss = { editing = null }, onSave = { vm.saveGoal(it); editing = null })
    }
    amountDialog?.let { (goal, deposit) ->
        AmountDialog(
            title = if (deposit) "Guardar em \"${goal.name}\"" else "Retirar de \"${goal.name}\"",
            onDismiss = { amountDialog = null },
            onConfirm = { cents -> vm.addToGoal(goal, if (deposit) cents else -cents); amountDialog = null },
        )
    }
    deleting?.let { g ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir meta?") },
            text = { Text("\"${g.name}\" será removida. Isso não pode ser desfeito.") },
            confirmButton = { TextButton(onClick = { vm.deleteGoal(g); deleting = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun GoalDialog(
    goal: GoalEntity?,
    presetName: String,
    presetTargetCents: Long,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit,
) {
    var name by remember { mutableStateOf(presetName) }
    var target by remember { mutableStateOf(if (presetTargetCents > 0) Money.formatPlain(presetTargetCents) else "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Nova meta" else "Editar meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome da meta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                MoneyField(value = target, onValueChange = { target = it; error = null }, label = "Valor da meta (R$)")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(target)
                if (name.isBlank()) {
                    error = "Informe o nome da meta."
                } else if (cents == null || cents <= 0L) {
                    error = "Informe um valor válido."
                } else {
                    val base = goal ?: GoalEntity(name = "", targetCents = 0L, savedCents = 0L)
                    onSave(base.copy(name = name.trim(), targetCents = cents))
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun AmountDialog(title: String, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                MoneyField(value = text, onValueChange = { text = it; error = false }, label = "Valor (R$)")
                if (error) Text("Informe um valor válido.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cents = Money.parse(text)
                if (cents == null || cents <= 0L) error = true else onConfirm(cents)
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
