package br.com.saudefinanceira.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.saudefinanceira.ui.components.AppCard
import br.com.saudefinanceira.ui.components.GradientProgress
import br.com.saudefinanceira.ui.components.ScreenHeader
import br.com.saudefinanceira.update.Installer
import br.com.saudefinanceira.update.UpdateState
import br.com.saudefinanceira.update.UpdateViewModel

@Composable
fun SettingsScreen(update: UpdateViewModel, padding: PaddingValues) {
    val st by update.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permissionHint by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
        ScreenHeader(title = "Ajustes", caption = "Versão instalada: ${update.installedName}")

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Atualizações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    when (val s = st) {
                        is UpdateState.Idle -> {
                            Text("Toque para ver se existe uma versão nova.", style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = { update.check(silent = false) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Verificar atualizações")
                            }
                        }
                        is UpdateState.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                                Text("Verificando...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        is UpdateState.UpToDate -> {
                            Text("Você já está na versão mais recente.", style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { update.check(silent = false) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Verificar de novo")
                            }
                        }
                        is UpdateState.Available -> {
                            val mb = s.info.sizeBytes / 1_048_576.0
                            Text(
                                "Nova versão disponível: ${s.info.tag}" + if (mb > 0) " (%.1f MB)".format(mb) else "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = { update.download() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Baixar atualização")
                            }
                        }
                        is UpdateState.Downloading -> {
                            Text("Baixando ${s.info.tag}... ${(s.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                            GradientProgress(s.progress)
                        }
                        is UpdateState.Ready -> {
                            Text(
                                "${s.info.tag} baixada e verificada. Toque em instalar e confirme na tela do Android.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(
                                onClick = { permissionHint = !Installer.install(context, s.file) },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Instalar agora") }
                            if (permissionHint) {
                                Text(
                                    "Permita que o Saúde Financeira instale apps na tela que abriu, volte e toque em Instalar agora de novo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        is UpdateState.Failed -> {
                            Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { update.reset(); update.check(silent = false) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Tentar de novo")
                            }
                        }
                    }
                }
            }

            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Privacidade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Seus dados financeiros ficam somente neste aparelho. A internet é usada apenas para consultar e baixar atualizações no GitHub.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
