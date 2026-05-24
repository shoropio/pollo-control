/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.domain.model.BatchReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PolloControlApp

    private val _reports = MutableStateFlow<List<BatchReport>>(emptyList())
    val reports: StateFlow<List<BatchReport>> = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun generateAllReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _reports.value = withContext(Dispatchers.Default) {
                    buildReportsFromCache()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildReportsFromCache(): List<BatchReport> {
        val batches = app.dataCache.batches.value
        val mortalities = app.dataCache.mortalities.value
        val feedings = app.dataCache.feedings.value
        val expenses = app.dataCache.expenses.value
        val weighings = app.dataCache.weighings.value
        val sales = app.dataCache.sales.value
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = System.currentTimeMillis()

        val mortalityByBatch = mortalities.groupingBy { it.loteId }.fold(0) { total, item -> total + item.cantidad }
        val feedByBatch = feedings.groupingBy { it.loteId }.fold(0.0) { total, item -> total + item.cantidadKg }
        val expensesByBatch = expenses
            .filter { it.loteId != null }
            .groupingBy { it.loteId ?: 0L }
            .fold(0.0) { total, item -> total + item.monto }
        val latestWeightByBatch = weighings
            .groupBy { it.loteId }
            .mapValues { (_, items) -> items.maxByOrNull { it.fecha }?.pesoPromedio ?: 0.0 }
        val salesByBatch = sales
            .filter { it.loteId != null }
            .groupBy { it.loteId ?: 0L }

        return batches.map { batch ->
            val mortality = mortalityByBatch[batch.id] ?: 0
            val totalFeed = feedByBatch[batch.id] ?: 0.0
            val totalExpenses = expensesByBatch[batch.id] ?: 0.0
            val avgWeight = latestWeightByBatch[batch.id] ?: 0.0
            val totalSales = salesByBatch[batch.id].orEmpty().sumOf { it.total }
            val alive = batch.cantidadInicial - mortality
            val profit = totalSales - totalExpenses

            BatchReport(
                batchName = batch.nombre,
                breed = batch.raza,
                entryDate = dateFormatter.format(Date(batch.fechaIngreso)),
                age = ((now - batch.fechaIngreso) / (1000 * 60 * 60 * 24)).toInt(),
                initialCount = batch.cantidadInicial,
                aliveCount = alive,
                totalMortality = mortality,
                mortalityPercentage = if (batch.cantidadInicial > 0) (mortality.toDouble() / batch.cantidadInicial) * 100 else 0.0,
                totalFeed = totalFeed,
                feedConversion = if (alive > 0 && avgWeight > 0) totalFeed / (alive * avgWeight) else 0.0,
                avgWeight = avgWeight,
                totalCost = totalExpenses,
                costPerChicken = if (alive > 0) totalExpenses / alive else 0.0,
                costPerKilo = if (alive > 0 && avgWeight > 0) totalExpenses / (alive * avgWeight) else 0.0,
                totalSales = totalSales,
                profit = profit,
                profitability = if (totalExpenses > 0) (profit / totalExpenses) * 100 else 0.0
            )
        }
    }

    fun getCsvContent(): String {
        return ReportExporter.exportToCsv(_reports.value, app.settingsManager.currency.value)
    }

    fun getPdfContent(): ByteArray {
        return ReportExporter.exportToPdf(_reports.value, app.settingsManager.currency.value)
    }
}
