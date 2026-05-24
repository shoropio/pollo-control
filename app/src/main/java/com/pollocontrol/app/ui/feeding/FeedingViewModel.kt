/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.ui.feeding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.BatchEntity
import kotlinx.coroutines.async
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
            // ✅ Ejecutar todas las queries en paralelo de forma sincronizada
            val batchDeferred = async { batchRepo.getById(batchId) }
            val recordsDeferred = async { repo.getByBatch(batchId).first() }
            val totalDeferred = async { repo.getTotalByBatch(batchId) }

            // Esperar todos los resultados
            _batch.value = batchDeferred.await()
            _records.value = recordsDeferred.await()
            _totalConsumption.value = totalDeferred.await()
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
