package com.pollocontrol.app.ui.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchViewModel @Inject constructor(
    private val batchRepository: BatchRepository
) : ViewModel() {

    val batches: StateFlow<List<BatchEntity>> = batchRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow("TODOS")
    val filteredBatches: StateFlow<List<BatchEntity>> = combine(batches, _filter) { list, filter ->
        if (filter == "TODOS") list else list.filter { it.estado == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) { _filter.value = filter }

    fun getById(id: Long, onResult: (BatchEntity?) -> Unit) {
        viewModelScope.launch { onResult(batchRepository.getById(id)) }
    }

    fun save(batch: BatchEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (batch.id == 0L) batchRepository.insert(batch) else batchRepository.update(batch)
            onSuccess()
        }
    }

    fun delete(batch: BatchEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            batchRepository.delete(batch)
            onSuccess()
        }
    }
}
