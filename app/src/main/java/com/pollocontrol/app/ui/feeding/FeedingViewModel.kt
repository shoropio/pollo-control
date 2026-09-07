/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.ui.feeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.domain.repository.FeedingRepository
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedingViewModel @Inject constructor(
    private val feedingRepository: FeedingRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _records = MutableStateFlow<List<FeedingEntity>>(emptyList())
    val records: StateFlow<List<FeedingEntity>> = _records.asStateFlow()

    private val _totalConsumption = MutableStateFlow(0.0)
    val totalConsumption: StateFlow<Double> = _totalConsumption.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            val batchDeferred = async { batchRepository.getById(batchId) }
            val recordsDeferred = async { feedingRepository.getByBatch(batchId).first() }
            val totalDeferred = async { feedingRepository.getTotalByBatch(batchId) }

            _batch.value = batchDeferred.await()
            _records.value = recordsDeferred.await()
            _totalConsumption.value = totalDeferred.await()
        }
    }

    fun save(record: FeedingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (record.id == 0L) feedingRepository.insert(record) else feedingRepository.update(record)
            onSuccess()
        }
    }

    fun delete(record: FeedingEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            feedingRepository.delete(record)
            onSuccess()
        }
    }
}
