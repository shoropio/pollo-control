package com.pollocontrol.app.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.domain.repository.SaleRepository
import com.pollocontrol.app.domain.repository.ClientRepository
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val clientRepository: ClientRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    val sales: StateFlow<List<SaleEntity>> = saleRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<ClientEntity>> = clientRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<BatchEntity>> = batchRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSales: StateFlow<Double> = sales
        .map { list -> list.sumOf { it.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun getById(id: Long, onResult: (SaleEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(saleRepository.getAll().first().find { it.id == id })
        }
    }

    fun save(sale: SaleEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (sale.id == 0L) saleRepository.insert(sale) else saleRepository.update(sale)
            if (sale.clienteId != null && (sale.estadoPago == "CREDITO" || sale.estadoPago == "PENDIENTE")) {
                val client = clientRepository.getById(sale.clienteId)
                if (client != null) {
                    clientRepository.update(client.copy(saldoPendiente = client.saldoPendiente + sale.total))
                }
            }
            onSuccess()
        }
    }

    fun delete(sale: SaleEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            saleRepository.delete(sale)
            onSuccess()
        }
    }
}
