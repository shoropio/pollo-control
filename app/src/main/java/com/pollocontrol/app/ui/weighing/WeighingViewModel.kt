package com.pollocontrol.app.ui.weighing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeighingViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.weighingRepository
    private val batchRepo = app.batchRepository

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<WeighingEntity>>(emptyList())
    val records: StateFlow<List<WeighingEntity>> = _records.asStateFlow()

    private val _latestWeighing = MutableStateFlow<WeighingEntity?>(null)
    val latestWeighing: StateFlow<WeighingEntity?> = _latestWeighing.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepo.getById(batchId)
            repo.getByBatch(batchId).collect { _records.value = it }
        }
        viewModelScope.launch {
            repo.getLatestFlow(batchId).collect { _latestWeighing.value = it }
        }
    }

    fun save(record: WeighingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) repo.insert(record) else repo.update(record)
            onSuccess()
        }
    }

    fun delete(record: WeighingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(record)
            onSuccess()
        }
    }
}
