package com.pollocontrol.app.ui.backuprestore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class BackupRestoreViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun performBackup(outputStream: OutputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                BackupManager.backup(getApplication(), outputStream)
                _message.value = "Copia de seguridad creada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al crear copia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun performRestore(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                BackupManager.restore(getApplication(), inputStream)
                _message.value = "Datos restaurados. Reinicie la aplicacion para aplicar cambios."
            } catch (e: Exception) {
                _message.value = "Error al restaurar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
