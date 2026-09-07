package com.pollocontrol.app.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import com.pollocontrol.app.domain.repository.TreatmentRepository
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val treatmentRepository: TreatmentRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _treatments = MutableStateFlow<List<TreatmentEntity>>(emptyList())
    val treatments: StateFlow<List<TreatmentEntity>> = _treatments.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepository.getById(batchId)
            treatmentRepository.getByBatch(batchId).collect { _treatments.value = it }
        }
    }

    fun save(treatment: TreatmentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (treatment.id == 0L) treatmentRepository.insert(treatment) else treatmentRepository.update(treatment)
            onSuccess()
        }
    }

    fun delete(treatment: TreatmentEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            treatmentRepository.delete(treatment)
            onSuccess()
        }
    }
}
