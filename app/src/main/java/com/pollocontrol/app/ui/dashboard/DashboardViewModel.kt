package com.pollocontrol.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import com.pollocontrol.app.domain.model.DashboardStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            @Suppress("UNCHECKED_CAST")
            combine(
                app.dataCache.batches,
                app.dataCache.mortalities,
                app.dataCache.expenses,
                app.dataCache.feedings,
                app.dataCache.weighings,
                app.dataCache.sales,
                app.dataCache.supplies
            ) { values ->
                buildStats(
                    batches = values[0] as List<BatchEntity>,
                    mortalities = values[1] as List<MortalityEntity>,
                    expenses = values[2] as List<ExpenseEntity>,
                    feedings = values[3] as List<FeedingEntity>,
                    weighings = values[4] as List<WeighingEntity>,
                    sales = values[5] as List<SaleEntity>,
                    supplies = values[6] as List<SupplyEntity>
                )
            }
                .flowOn(Dispatchers.Default)
                .collect { dashboardStats ->
                    _stats.value = dashboardStats
                    _isLoading.value = false
                }
        }
    }

    fun loadDashboard(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            _stats.value = buildStats(
                batches = app.dataCache.batches.value,
                mortalities = app.dataCache.mortalities.value,
                expenses = app.dataCache.expenses.value,
                feedings = app.dataCache.feedings.value,
                weighings = app.dataCache.weighings.value,
                sales = app.dataCache.sales.value,
                supplies = app.dataCache.supplies.value
            )
            _isLoading.value = false
        }
    }

    private fun buildStats(
        batches: List<BatchEntity>,
        mortalities: List<MortalityEntity>,
        expenses: List<ExpenseEntity>,
        feedings: List<FeedingEntity>,
        weighings: List<WeighingEntity>,
        sales: List<SaleEntity>,
        supplies: List<SupplyEntity>
    ): DashboardStats {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val weekStart = calendar.apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
        val monthStart = calendar.apply { add(Calendar.MONTH, -1) }.timeInMillis

        val activeBatches = batches.filter { it.estado == "ACTIVO" }
        val mortalityByBatch = mortalities.groupingBy { it.loteId }.fold(0) { total, item -> total + item.cantidad }
        val expensesByBatch = expenses
            .filter { it.loteId != null }
            .groupingBy { it.loteId ?: 0L }
            .fold(0.0) { total, item -> total + item.monto }
        val feedByBatch = feedings.groupingBy { it.loteId }.fold(0.0) { total, item -> total + item.cantidadKg }
        val latestWeightByBatch = weighings
            .groupBy { it.loteId }
            .mapValues { (_, items) -> items.maxByOrNull { it.fecha }?.pesoPromedio ?: 0.0 }

        var totalMortality = 0
        var totalAlive = 0
        var totalBatchExpenses = 0.0
        var totalFeedConsumed = 0.0
        var latestWeight = 0.0

        activeBatches.forEach { batch ->
            val mortality = mortalityByBatch[batch.id] ?: 0
            totalMortality += mortality
            totalAlive += batch.cantidadInicial - mortality
            totalBatchExpenses += expensesByBatch[batch.id] ?: 0.0
            totalFeedConsumed += feedByBatch[batch.id] ?: 0.0
            latestWeight = maxOf(latestWeight, latestWeightByBatch[batch.id] ?: 0.0)
        }

        val generalExpenses = expenses.filter { it.loteId == null }.sumOf { it.monto }
        val totalExpenses = totalBatchExpenses + generalExpenses
        val totalSales = sales.sumOf { it.total }
        val salesToday = sales.filter { it.fecha in todayStart..now }.sumOf { it.total }
        val salesWeek = sales.filter { it.fecha in weekStart..now }.sumOf { it.total }
        val salesMonth = sales.filter { it.fecha in monthStart..now }.sumOf { it.total }
        val pendingSales = sales.count { it.estadoPago == "PENDIENTE" || it.estadoPago == "CREDITO" }
        val lowStockItems = supplies.count { it.stockActual <= it.stockMinimo }
        val initialActiveCount = activeBatches.sumOf { it.cantidadInicial }

        return DashboardStats(
            activeLotes = activeBatches.size,
            totalLiveChickens = totalAlive,
            totalMortality = totalMortality,
            totalExpenses = totalExpenses,
            totalSales = totalSales,
            costPerChicken = if (totalAlive > 0) totalExpenses / totalAlive else 0.0,
            costPerKilo = if (totalFeedConsumed > 0) totalExpenses / (totalAlive * (if (latestWeight > 0) latestWeight else 1.0)) else 0.0,
            estimatedProfit = totalSales - totalExpenses,
            salesToday = salesToday,
            salesWeek = salesWeek,
            salesMonth = salesMonth,
            lowStockItems = lowStockItems,
            pendingPayments = pendingSales,
            mortalityRate = if (initialActiveCount > 0) (totalMortality.toDouble() / initialActiveCount * 100) else 0.0,
            feedConversion = if (totalFeedConsumed > 0 && totalAlive > 0 && latestWeight > 0) totalFeedConsumed / (totalAlive * latestWeight) else 0.0,
            averageWeight = latestWeight,
            totalFeedConsumed = totalFeedConsumed
        )
    }
}
