package com.pollocontrol.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.domain.repository.ExpenseRepository
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    val expenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _batchMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val batchMap: StateFlow<Map<Long, String>> = _batchMap.asStateFlow()

    init {
        viewModelScope.launch {
            expenseRepository.getAll().collect { list ->
                _totalExpenses.value = list.sumOf { it.monto }
            }
        }
        viewModelScope.launch {
            batchRepository.getAll().collect { batches ->
                _batchMap.value = batches.associate { it.id to it.nombre }
            }
        }
    }

    fun getById(id: Long, onResult: (ExpenseEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(expenseRepository.getAll().first().find { it.id == id })
        }
    }

    fun save(expense: ExpenseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (expense.id == 0L) expenseRepository.insert(expense) else expenseRepository.update(expense)
            onSuccess()
        }
    }

    fun delete(expense: ExpenseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.delete(expense)
            onSuccess()
        }
    }
}
