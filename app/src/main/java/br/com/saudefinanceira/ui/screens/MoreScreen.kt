package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.ScreenHeader

/** Aba "Mais": o que não precisa ficar na frente no dia a dia. */
@Composable
fun MoreScreen(padding: PaddingValues, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Mais", caption = "Outras coisas que o app faz")
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MoreItem("Resumo do mês", "Quanto sobra e para onde vai o seu dinheiro") { onOpen("resumo") }
            MoreItem("Compras parceladas", "Cartão, crediário e vale") { onOpen("parcelas") }
            MoreItem("Metas, dívidas e dicas", "Quanto você já guardou e como sair do aperto") { onOpen("dicas") }
            MoreItem("Relatórios", "Compare os meses e salve numa planilha") { onOpen("relatorios") }
            MoreItem("Ajustes", "Lembretes, atualização do app e cópia de segurança") { onOpen("ajustes") }
        }
    }
}

@Composable
private fun MoreItem(title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
