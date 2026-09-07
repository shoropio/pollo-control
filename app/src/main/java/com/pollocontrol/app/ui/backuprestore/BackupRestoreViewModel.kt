package com.pollocontrol.app.ui.backuprestore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun performBackup(outputStream: OutputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                BackupManager.backup(context, outputStream)
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
                BackupManager.restore(context, inputStream)
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
