package com.pollocontrol.app.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.domain.repository.ClientRepository
import com.pollocontrol.app.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    val clients: StateFlow<List<ClientEntity>> = clientRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _query = MutableStateFlow("")
    val searchResults: StateFlow<List<ClientEntity>> = combine(clients, _query) { list, q ->
        if (q.isBlank()) list else list.filter { it.nombre.contains(q, ignoreCase = true) || it.telefono.contains(q) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clientSales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val clientSales: StateFlow<List<SaleEntity>> = _clientSales.asStateFlow()

    fun search(query: String) { _query.value = query }

    fun getById(id: Long, onResult: (ClientEntity?) -> Unit) {
        viewModelScope.launch { onResult(clientRepository.getById(id)) }
    }

    fun save(client: ClientEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (client.id == 0L) clientRepository.insert(client) else clientRepository.update(client)
            onSuccess()
        }
    }

    fun delete(client: ClientEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            clientRepository.delete(client)
            onSuccess()
        }
    }

    fun loadClientSales(clientId: Long) {
        viewModelScope.launch {
            saleRepository.getByClient(clientId).collect { _clientSales.value = it }
        }
    }
}
