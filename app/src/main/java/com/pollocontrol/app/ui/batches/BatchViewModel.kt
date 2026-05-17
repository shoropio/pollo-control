package com.pollocontrol.app.ui.batches

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BatchViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.batchRepository

    val batches: StateFlow<List<BatchEntity>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow("TODOS")
    val filteredBatches: StateFlow<List<BatchEntity>> = combine(batches, _filter) { list, filter ->
        if (filter == "TODOS") list else list.filter { it.estado == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) { _filter.value = filter }

    fun getById(id: Long, onResult: (BatchEntity?) -> Unit) {
        viewModelScope.launch { onResult(repo.getById(id)) }
    }

    fun save(batch: BatchEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (batch.id == 0L) repo.insert(batch) else repo.update(batch)
            onSuccess()
        }
    }

    fun delete(batch: BatchEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(batch)
            onSuccess()
        }
    }
}
