package br.com.saudefinanceira.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.saudefinanceira.FinanceApp
import br.com.saudefinanceira.data.Backup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class BackupViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as FinanceApp).db
    private val resolver = app.contentResolver

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    fun export(uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val json = Backup.toJson(Backup.snapshot(db))
                    val out = resolver.openOutputStream(uri, "wt") ?: throw IOException("Sem acesso ao arquivo.")
                    out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                _message.value = "Backup salvo com sucesso."
            } catch (e: Exception) {
                _message.value = "Não foi possível salvar o backup."
            }
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val input = resolver.openInputStream(uri) ?: throw IOException("Sem acesso ao arquivo.")
                    val text = input.use { it.readBytes().toString(Charsets.UTF_8) }
                    val data = Backup.parse(text) // valida tudo antes de mexer no banco
                    Backup.replaceAll(db, data)
                }
                _message.value = "Backup restaurado com sucesso."
            } catch (e: Exception) {
                _message.value = "Não foi possível restaurar. Confirme se o arquivo é um backup do Saúde Financeira. Nada foi alterado."
            }
        }
    }
}
