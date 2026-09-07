package com.pollocontrol.app.ui.mortality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.MortalityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MortalityViewModel @Inject constructor(
    private val mortalityRepository: MortalityRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _mortalityRecords = MutableStateFlow<List<MortalityEntity>>(emptyList())
    val mortalityRecords: StateFlow<List<MortalityEntity>> = _mortalityRecords.asStateFlow()

    private val _totalMortality = MutableStateFlow(0)
    val totalMortality: StateFlow<Int> = _totalMortality.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepository.getById(batchId)
            mortalityRepository.getByBatch(batchId).collect { list ->
                _mortalityRecords.value = list
            }
        }
        viewModelScope.launch {
            mortalityRepository.getTotalByBatchFlow(batchId).collect { total ->
                _totalMortality.value = total
            }
        }
    }

    fun getAliveCount(): Int = (_batch.value?.cantidadInicial ?: 0) - _totalMortality.value

    fun save(mortality: MortalityEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (mortality.id == 0L) mortalityRepository.insert(mortality) else mortalityRepository.update(mortality)
            onSuccess()
        }
    }

    fun delete(mortality: MortalityEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            mortalityRepository.delete(mortality)
            onSuccess()
        }
    }
}
