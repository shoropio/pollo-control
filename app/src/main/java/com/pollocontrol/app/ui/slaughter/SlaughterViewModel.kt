package com.pollocontrol.app.ui.slaughter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SlaughterViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.slaughterRepository
    private val batchRepo = app.batchRepository

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<SlaughterEntity>>(emptyList())
    val records: StateFlow<List<SlaughterEntity>> = _records.asStateFlow()

    private val _totalSlaughtered = MutableStateFlow(0)
    val totalSlaughtered: StateFlow<Int> = _totalSlaughtered.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepo.getById(batchId)
            repo.getByBatch(batchId).collect { _records.value = it }
        }
        viewModelScope.launch {
            _totalSlaughtered.value = repo.getTotalByBatch(batchId)
        }
    }

    fun save(record: SlaughterEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) repo.insert(record) else repo.update(record)
            onSuccess()
        }
    }

    fun delete(record: SlaughterEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(record)
            onSuccess()
        }
    }
}
