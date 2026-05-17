package com.pollocontrol.app.ui.feeding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FeedingViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.feedingRepository
    private val batchRepo = app.batchRepository

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<FeedingEntity>>(emptyList())
    val records: StateFlow<List<FeedingEntity>> = _records.asStateFlow()

    private val _totalConsumption = MutableStateFlow(0.0)
    val totalConsumption: StateFlow<Double> = _totalConsumption.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepo.getById(batchId)
            repo.getByBatch(batchId).collect { _records.value = it }
        }
        viewModelScope.launch {
            _totalConsumption.value = repo.getTotalByBatch(batchId)
        }
    }

    fun save(record: FeedingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) repo.insert(record) else repo.update(record)
            onSuccess()
        }
    }

    fun delete(record: FeedingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(record)
            onSuccess()
        }
    }
}
