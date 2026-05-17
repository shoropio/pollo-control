package com.pollocontrol.app.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClientsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.clientRepository
    private val saleRepo = app.saleRepository

    val clients: StateFlow<List<ClientEntity>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _query = MutableStateFlow("")
    val searchResults: StateFlow<List<ClientEntity>> = combine(clients, _query) { list, q ->
        if (q.isBlank()) list else list.filter { it.nombre.contains(q, ignoreCase = true) || it.telefono.contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clientSales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val clientSales: StateFlow<List<SaleEntity>> = _clientSales.asStateFlow()

    fun search(query: String) { _query.value = query }

    fun getById(id: Long, onResult: (ClientEntity?) -> Unit) {
        viewModelScope.launch { onResult(repo.getById(id)) }
    }

    fun save(client: ClientEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (client.id == 0L) repo.insert(client) else repo.update(client)
            onSuccess()
        }
    }

    fun delete(client: ClientEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(client)
            onSuccess()
        }
    }

    fun loadClientSales(clientId: Long) {
        viewModelScope.launch {
            saleRepo.getByClient(clientId).collect { _clientSales.value = it }
        }
    }
}
