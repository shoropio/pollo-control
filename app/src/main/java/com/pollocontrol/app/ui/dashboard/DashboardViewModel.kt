package com.pollocontrol.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.domain.model.DashboardStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var lastLoadedAt = 0L

    init { loadDashboard() }

    fun loadDashboard(forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && !_isLoading.value && now - lastLoadedAt < CACHE_WINDOW_MS) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val batchRepo = app.batchRepository
                val mortalityRepo = app.mortalityRepository
                val expenseRepo = app.expenseRepository
                val saleRepo = app.saleRepository
                val supplyRepo = app.supplyRepository
                val weighingRepo = app.weighingRepository
                val feedingRepo = app.feedingRepository

                val calendar = Calendar.getInstance()
                val todayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val weekStart = calendar.apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
                val monthStart = calendar.apply { add(Calendar.MONTH, -1) }.timeInMillis

                val batches = batchRepo.getAll().first()
                val activeBatches = batches.filter { it.estado == "ACTIVO" }

                val batchStats = activeBatches.map { batch ->
                    async {
                        val mortalityDeferred = async { mortalityRepo.getTotalByBatch(batch.id) }
                        val expensesDeferred = async { expenseRepo.getTotalByBatch(batch.id) }
                        val feedDeferred = async { feedingRepo.getTotalByBatch(batch.id) }
                        val weightDeferred = async { weighingRepo.getLatest(batch.id) }

                        val mort = mortalityDeferred.await()
                        val expenses = expensesDeferred.await()
                        val feed = feedDeferred.await()
                        val weight = weightDeferred.await()

                        BatchDashboardData(
                            mortality = mort,
                            alive = batch.cantidadInicial - mort,
                            expenses = expenses,
                            feedConsumed = feed,
                            latestWeight = weight?.pesoPromedio ?: 0.0
                        )
                    }
                }.map { it.await() }

                val totalMortality = batchStats.sumOf { it.mortality }
                val totalAlive = batchStats.sumOf { it.alive }
                val totalExpensesBatches = batchStats.sumOf { it.expenses }
                val totalFeedConsumed = batchStats.sumOf { it.feedConsumed }
                val latestWeight = batchStats.maxOfOrNull { it.latestWeight } ?: 0.0

                val generalExpensesDeferred = async { expenseRepo.getTotalGeneral() }
                val totalSalesDeferred = async { saleRepo.getTotal() }
                val salesTodayDeferred = async { saleRepo.getTotalBetweenDates(todayStart, System.currentTimeMillis()) }
                val salesWeekDeferred = async { saleRepo.getTotalBetweenDates(weekStart, System.currentTimeMillis()) }
                val salesMonthDeferred = async { saleRepo.getTotalBetweenDates(monthStart, System.currentTimeMillis()) }
                val lowStockDeferred = async { supplyRepo.getLowStock().first() }
                val pendingSalesDeferred = async { saleRepo.getPending().first() }

                val generalExpenses = generalExpensesDeferred.await()
                val totalSales = totalSalesDeferred.await()
                val salesToday = salesTodayDeferred.await()
                val salesWeek = salesWeekDeferred.await()
                val salesMonth = salesMonthDeferred.await()
                val lowStockItems = lowStockDeferred.await().size
                val pendingSales = pendingSalesDeferred.await().size

                val totalExpenses = totalExpensesBatches + generalExpenses
                val costPerChicken = if (totalAlive > 0) totalExpenses / totalAlive else 0.0
                val costPerKilo = if (totalFeedConsumed > 0) totalExpenses / (totalAlive * (if (latestWeight > 0) latestWeight else 1.0)) else 0.0
                val estimatedProfit = totalSales - totalExpenses
                val mortalityRate = if (activeBatches.isNotEmpty() && activeBatches.sumOf { it.cantidadInicial } > 0)
                    (totalMortality.toDouble() / activeBatches.sumOf { it.cantidadInicial } * 100) else 0.0

                _stats.value = DashboardStats(
                    activeLotes = activeBatches.size,
                    totalLiveChickens = totalAlive,
                    totalMortality = totalMortality,
                    totalExpenses = totalExpenses,
                    totalSales = totalSales,
                    costPerChicken = costPerChicken,
                    costPerKilo = costPerKilo,
                    estimatedProfit = estimatedProfit,
                    salesToday = salesToday,
                    salesWeek = salesWeek,
                    salesMonth = salesMonth,
                    lowStockItems = lowStockItems,
                    pendingPayments = pendingSales,
                    mortalityRate = mortalityRate,
                    feedConversion = if (totalFeedConsumed > 0 && totalAlive > 0 && latestWeight > 0) totalFeedConsumed / (totalAlive * latestWeight) else 0.0,
                    averageWeight = latestWeight,
                    totalFeedConsumed = totalFeedConsumed
                )
                lastLoadedAt = System.currentTimeMillis()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private data class BatchDashboardData(
        val mortality: Int,
        val alive: Int,
        val expenses: Double,
        val feedConsumed: Double,
        val latestWeight: Double
    )

    companion object {
        private const val CACHE_WINDOW_MS = 30_000L
    }
}
