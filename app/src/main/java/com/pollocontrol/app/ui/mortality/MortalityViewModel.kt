package com.pollocontrol.app.ui.mortality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MortalityViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val mortalityRepo = app.mortalityRepository
    private val batchRepo = app.batchRepository

    private val _batch = MutableStateFlow<BatchEntity?>(null)
    val batch: StateFlow<BatchEntity?> = _batch.asStateFlow()

    private val _mortalityRecords = MutableStateFlow<List<MortalityEntity>>(emptyList())
    val mortalityRecords: StateFlow<List<MortalityEntity>> = _mortalityRecords.asStateFlow()

    private val _totalMortality = MutableStateFlow(0)
    val totalMortality: StateFlow<Int> = _totalMortality.asStateFlow()

    fun loadBatch(batchId: Long) {
        viewModelScope.launch {
            _batch.value = batchRepo.getById(batchId)
            mortalityRepo.getByBatch(batchId).collect { list ->
                _mortalityRecords.value = list
            }
        }
        viewModelScope.launch {
            mortalityRepo.getTotalByBatchFlow(batchId).collect { total ->
                _totalMortality.value = total
            }
        }
    }

    fun getAliveCount(): Int = (_batch.value?.cantidadInicial ?: 0) - _totalMortality.value

    fun save(mortality: MortalityEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (mortality.id == 0L) mortalityRepo.insert(mortality) else mortalityRepo.update(mortality)
            onSuccess()
        }
    }

    fun delete(mortality: MortalityEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            mortalityRepo.delete(mortality)
            onSuccess()
        }
    }
}
