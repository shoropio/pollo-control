package com.pollocontrol.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import com.pollocontrol.app.domain.repository.SupplyRepository
import com.pollocontrol.app.domain.repository.SupplyMovementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val supplyRepository: SupplyRepository,
    private val supplyMovementRepository: SupplyMovementRepository
) : ViewModel() {

    val supplies: StateFlow<List<SupplyEntity>> = supplyRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow("TODOS")
    val filteredSupplies: StateFlow<List<SupplyEntity>> = combine(supplies, _filter) { list, filter ->
        if (filter == "TODOS") list else list.filter { it.tipo == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockSupplies: StateFlow<List<SupplyEntity>> = supplyRepository.getLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) { _filter.value = filter }

    fun getById(id: Long, onResult: (SupplyEntity?) -> Unit) {
        viewModelScope.launch { onResult(supplyRepository.getById(id)) }
    }

    fun save(supply: SupplyEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (supply.id == 0L) supplyRepository.insert(supply) else supplyRepository.update(supply)
            onSuccess()
        }
    }

    fun addStock(supplyId: Long, amount: Double, cost: Double?, notes: String) {
        viewModelScope.launch {
            supplyRepository.addStock(supplyId, amount)
            supplyMovementRepository.insert(SupplyMovementEntity(
                insumoId = supplyId, tipo = "ENTRADA", cantidad = amount,
                fecha = System.currentTimeMillis(), costo = cost, observaciones = notes
            ))
        }
    }

    fun removeStock(supplyId: Long, amount: Double, notes: String) {
        viewModelScope.launch {
            supplyRepository.removeStock(supplyId, amount)
            supplyMovementRepository.insert(SupplyMovementEntity(
                insumoId = supplyId, tipo = "SALIDA", cantidad = amount,
                fecha = System.currentTimeMillis(), observaciones = notes
            ))
        }
    }

    fun delete(supply: SupplyEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supplyRepository.delete(supply)
            onSuccess()
        }
    }
}
