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
    GuidePage("Quanto posso gastar hoje", "A tela Hoje mostra quanto você ainda pode gastar no dia. Gastou algo? Toque em Gastei e digite o valor. O app ajusta o limite na hora."),
    GuidePage("Suas contas", "Na aba Contas, cadastre aluguel, luz, água e o resto. O app mostra em que ordem pagar quando o dinheiro não dá para tudo."),
    GuidePage("Mais", "Na aba Mais ficam o resumo do mês, as compras parceladas, as metas, as dívidas, os relatórios e os ajustes (lembretes e cópia de segurança)."),
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
