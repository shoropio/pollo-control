package com.pollocontrol.app.ui.slaughter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.SlaughterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlaughterViewModel @Inject constructor(
    private val slaughterRepository: SlaughterRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<SlaughterEntity>>(emptyList())
    val records: StateFlow<List<SlaughterEntity>> = _records.asStateFlow()

    private val _totalSlaughtered = MutableStateFlow(0)
    val totalSlaughtered: StateFlow<Int> = _totalSlaughtered.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepository.getById(batchId)
            slaughterRepository.getByBatch(batchId).collect { _records.value = it }
        }
        viewModelScope.launch {
            _totalSlaughtered.value = slaughterRepository.getTotalByBatch(batchId)
        }
    }

    fun save(record: SlaughterEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) slaughterRepository.insert(record) else slaughterRepository.update(record)
            onSuccess()
        }
    }

    fun delete(record: SlaughterEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            slaughterRepository.delete(record)
            onSuccess()
        }
    }
}
