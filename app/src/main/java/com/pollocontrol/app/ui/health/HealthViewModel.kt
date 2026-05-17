package com.pollocontrol.app.ui.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.treatmentRepository
    private val batchRepo = app.batchRepository

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _treatments = MutableStateFlow<List<TreatmentEntity>>(emptyList())
    val treatments: StateFlow<List<TreatmentEntity>> = _treatments.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepo.getById(batchId)
            repo.getByBatch(batchId).collect { _treatments.value = it }
        }
    }

    fun save(treatment: TreatmentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (treatment.id == 0L) repo.insert(treatment) else repo.update(treatment)
            onSuccess()
        }
    }

    fun delete(treatment: TreatmentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(treatment)
            onSuccess()
        }
    }
}
