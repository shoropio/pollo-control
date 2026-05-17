package com.pollocontrol.app.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp
    private val repo = app.expenseRepository
    private val batchRepo = app.batchRepository

    val expenses: StateFlow<List<ExpenseEntity>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _batchMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val batchMap: StateFlow<Map<Long, String>> = _batchMap.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { list ->
                _totalExpenses.value = list.sumOf { it.monto }
            }
        }
        viewModelScope.launch {
            batchRepo.getAll().collect { batches ->
                _batchMap.value = batches.associate { it.id to it.nombre }
            }
        }
    }

    fun getById(id: Long, onResult: (ExpenseEntity?) -> Unit) {
        viewModelScope.launch {
            onResult(repo.getAll().first().find { it.id == id })
        }
    }

    fun save(expense: ExpenseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (expense.id == 0L) repo.insert(expense) else repo.update(expense)
            onSuccess()
        }
    }

    fun delete(expense: ExpenseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.delete(expense)
            onSuccess()
        }
    }
}
