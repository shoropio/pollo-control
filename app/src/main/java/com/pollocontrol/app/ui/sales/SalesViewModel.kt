package com.pollocontrol.app.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val saleRepo = app.saleRepository
    private val clientRepo = app.clientRepository
    private val batchRepo = app.batchRepository

    val sales: StateFlow<List<SaleEntity>> = saleRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clients = MutableStateFlow<List<ClientEntity>>(emptyList())
    val clients: StateFlow<List<ClientEntity>> = _clients.asStateFlow()

    private val _batches = MutableStateFlow<List<BatchEntity>>(emptyList())
    val batches: StateFlow<List<BatchEntity>> = _batches.asStateFlow()

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales.asStateFlow()

    init {
        viewModelScope.launch {
            clientRepo.getAll().collect { _clients.value = it }
        }
        viewModelScope.launch {
            batchRepo.getAll().collect { _batches.value = it }
        }
        viewModelScope.launch {
            saleRepo.getAll().collect { list ->
                _totalSales.value = list.sumOf { it.total }
            }
        }
    }

    fun getById(id: Long, onResult: (SaleEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(saleRepo.getAll().first().find { it.id == id })
        }
    }

    fun save(sale: SaleEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (sale.id == 0L) saleRepo.insert(sale) else saleRepo.update(sale)
            if (sale.clienteId != null && (sale.estadoPago == "CREDITO" || sale.estadoPago == "PENDIENTE")) {
                val client = clientRepo.getById(sale.clienteId)
                if (client != null) {
                    clientRepo.update(client.copy(saldoPendiente = client.saldoPendiente + sale.total))
                }
            }
            onSuccess()
        }
    }

    fun delete(sale: SaleEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            saleRepo.delete(sale)
            onSuccess()
        }
    }
}
