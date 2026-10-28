package br.com.saudefinanceira.update

import android.app.Application
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

class UpdateViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state

    private val pkg = app.packageManager.getPackageInfo(app.packageName, 0)
    val installedCode: Long = PackageInfoCompat.getLongVersionCode(pkg)
    val installedName: String = pkg.versionName ?: "?"

    private val updatesDir = File(app.cacheDir, "updates")

    /** [silent]: verificação automática ao abrir o app, sem mostrar erros. */
    fun check(silent: Boolean) {
        val s = _state.value
        if (s is UpdateState.Checking || s is UpdateState.Downloading || s is UpdateState.Ready) return
        viewModelScope.launch {
            if (!silent) _state.value = UpdateState.Checking
            try {
                val info = withContext(Dispatchers.IO) { Updater.fetchLatest() }
                _state.value =
                    if (info.versionCode > installedCode) UpdateState.Available(info) else UpdateState.UpToDate
            } catch (e: Exception) {
                if (!silent) {
                    _state.value = UpdateState.Failed("Não foi possível verificar agora. Confira a internet e tente de novo.")
                }
            }
        }
    }

    fun download() {
        val info = (_state.value as? UpdateState.Available)?.info ?: return
        viewModelScope.launch {
            _state.value = UpdateState.Downloading(info, 0f)
            try {
                val file = withContext(Dispatchers.IO) {
                    Updater.downloadVerified(updatesDir, info) { p ->
                        _state.value = UpdateState.Downloading(info, p)
                    }
                }
                _state.value = UpdateState.Ready(info, file)
            } catch (e: IntegrityException) {
                _state.value = UpdateState.Failed("O arquivo não passou na verificação de integridade. A atualização foi cancelada.")
            } catch (e: Exception) {
                _state.value = UpdateState.Failed("Falha ao baixar a atualização. Confira a internet e tente de novo.")
            }
        }
    }

    fun reset() {
        _state.value = UpdateState.Idle
    }
}
