package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.domain.TipEngine
import br.com.saudefinanceira.domain.TipLevel
import br.com.saudefinanceira.ui.FinanceViewModel
import br.com.saudefinanceira.ui.components.ScreenHeader

@Composable
fun TipsScreen(vm: FinanceViewModel, padding: PaddingValues) {
    val s by vm.snapshot.collectAsStateWithLifecycle()
    val tips = TipEngine.generate(s)

    Column(Modifier.fillMaxSize().padding(padding)) {
        ScreenHeader(title = "Dicas para você", caption = "Calculadas no seu aparelho, com base nos seus números")
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tips) { tip ->
                val container = when (tip.level) {
                    TipLevel.ALERT -> MaterialTheme.colorScheme.errorContainer
                    TipLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                    TipLevel.GOOD -> MaterialTheme.colorScheme.primaryContainer
                    TipLevel.INFO -> MaterialTheme.colorScheme.surface
                }
                Card(
                    Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = container, contentColor = MaterialTheme.colorScheme.onSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tip.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(tip.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
