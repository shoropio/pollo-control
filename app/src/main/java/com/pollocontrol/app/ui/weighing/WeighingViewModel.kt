package com.pollocontrol.app.ui.weighing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.WeighingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeighingViewModel @Inject constructor(
    private val weighingRepository: WeighingRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<WeighingEntity>>(emptyList())
    val records: StateFlow<List<WeighingEntity>> = _records.asStateFlow()

    private val _latestWeighing = MutableStateFlow<WeighingEntity?>(null)
    val latestWeighing: StateFlow<WeighingEntity?> = _latestWeighing.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepository.getById(batchId)
            weighingRepository.getByBatch(batchId).collect { _records.value = it }
        }
        viewModelScope.launch {
            weighingRepository.getLatestFlow(batchId).collect { _latestWeighing.value = it }
        }
    }

    fun save(record: WeighingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) weighingRepository.insert(record) else weighingRepository.update(record)
            onSuccess()
        }
    }

    fun delete(record: WeighingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            weighingRepository.delete(record)
            onSuccess()
        }
    }
}
