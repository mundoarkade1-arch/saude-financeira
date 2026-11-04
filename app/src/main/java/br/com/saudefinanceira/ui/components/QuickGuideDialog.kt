package br.com.saudefinanceira.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class GuidePage(val title: String, val description: String)

private val guidePages = listOf(
    GuidePage("Navegue pelo app", "A barra inferior leva a Início (resumo do mês), Contas, Parcelas, Dicas e Ajustes. No Início, veja a sobra prevista, os compromissos e a divisão dos gastos."),
    GuidePage("Contas", "Cadastre contas fixas, como aluguel, e variáveis, como luz e mercado. Em Contas variáveis, toque em Histórico para corrigir ou incluir um mês."),
    GuidePage("Parcelas", "Informe o valor total, a quantidade e o mês da primeira parcela. O app calcula o andamento e o impacto nos próximos meses; faturas por cartão aparecem nessa mesma tela."),
    GuidePage("Dicas e metas", "As dicas ajudam a interpretar seus números. Em Metas, acompanhe objetivos e registre o quanto já guardou."),
    GuidePage("Relatórios e ajustes", "No Início, abra Relatórios para comparar períodos e exportar CSV. Em Ajustes, configure lembretes, atualização e backup."),
)

@Composable
fun QuickGuideDialog(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val current = guidePages[page]
    AlertDialog(
        onDismissRequest = onFinish,
        title = { Text("Guia rápido do app") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${page + 1} de ${guidePages.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(
                    progress = (page + 1).toFloat() / guidePages.size,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(current.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(current.description, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (page == guidePages.lastIndex) onFinish() else page++ }) {
                Text(if (page == guidePages.lastIndex) "Começar a usar" else "Próximo")
            }
        },
        dismissButton = { TextButton(onClick = onFinish) { Text("Pular guia") } },
    )
}
