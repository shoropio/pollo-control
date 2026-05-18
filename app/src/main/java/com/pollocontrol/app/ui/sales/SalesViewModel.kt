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

    val clients: StateFlow<List<ClientEntity>> = clientRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<BatchEntity>> = batchRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSales: StateFlow<Double> = sales
        .map { list -> list.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
