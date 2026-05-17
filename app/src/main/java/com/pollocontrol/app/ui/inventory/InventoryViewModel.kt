package com.pollocontrol.app.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val supplyRepo = app.supplyRepository
    private val movRepo = app.supplyMovementRepository

    val supplies: StateFlow<List<SupplyEntity>> = supplyRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow("TODOS")
    val filteredSupplies: StateFlow<List<SupplyEntity>> = combine(supplies, _filter) { list, filter ->
        if (filter == "TODOS") list else list.filter { it.tipo == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockSupplies: StateFlow<List<SupplyEntity>> = supplyRepo.getLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) { _filter.value = filter }

    fun getById(id: Long, onResult: (SupplyEntity?) -> Unit) {
        viewModelScope.launch { onResult(supplyRepo.getById(id)) }
    }

    fun save(supply: SupplyEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (supply.id == 0L) supplyRepo.insert(supply) else supplyRepo.update(supply)
            onSuccess()
        }
    }

    fun addStock(supplyId: Long, amount: Double, cost: Double?, notes: String) {
        viewModelScope.launch {
            supplyRepo.addStock(supplyId, amount)
            movRepo.insert(SupplyMovementEntity(
                insumoId = supplyId, tipo = "ENTRADA", cantidad = amount,
                fecha = System.currentTimeMillis(), costo = cost, observaciones = notes
            ))
        }
    }

    fun removeStock(supplyId: Long, amount: Double, notes: String) {
        viewModelScope.launch {
            supplyRepo.removeStock(supplyId, amount)
            movRepo.insert(SupplyMovementEntity(
                insumoId = supplyId, tipo = "SALIDA", cantidad = amount,
                fecha = System.currentTimeMillis(), observaciones = notes
            ))
        }
    }

    fun delete(supply: SupplyEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            supplyRepo.delete(supply)
            onSuccess()
        }
    }
}
