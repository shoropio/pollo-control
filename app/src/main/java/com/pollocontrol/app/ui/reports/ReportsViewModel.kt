package com.pollocontrol.app.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.domain.model.BatchReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                val batches = app.batchRepository.getAll().first()
                val reports = batches.map { batch ->
                    val mortality = app.mortalityRepository.getTotalByBatch(batch.id)
                    val alive = batch.cantidadInicial - mortality
                    val totalFeed = app.feedingRepository.getTotalByBatch(batch.id)
                    val totalExpenses = app.expenseRepository.getTotalByBatch(batch.id)
                    val totalSales = app.saleRepository.getAll().first()
                        .filter { it.loteId == batch.id }.sumOf { it.total }
                    val latestWeighing = app.weighingRepository.getLatest(batch.id)
                    val avgWeight = latestWeighing?.pesoPromedio ?: 0.0
                    val conversion = if (alive > 0 && avgWeight > 0) totalFeed / (alive * avgWeight) else 0.0
                    val costPerChicken = if (alive > 0) totalExpenses / alive else 0.0
                    val costPerKilo = if (alive > 0 && avgWeight > 0) totalExpenses / (alive * avgWeight) else 0.0
                    val profit = totalSales - totalExpenses
                    val profitability = if (totalExpenses > 0) (profit / totalExpenses) * 100 else 0.0
                    val mortalityPct = if (batch.cantidadInicial > 0) (mortality.toDouble() / batch.cantidadInicial) * 100 else 0.0
                    val age = ((System.currentTimeMillis() - batch.fechaIngreso) / (1000 * 60 * 60 * 24)).toInt()

                    BatchReport(
                        batchName = batch.nombre,
                        breed = batch.raza,
                        entryDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(batch.fechaIngreso)),
                        age = age,
                        initialCount = batch.cantidadInicial,
                        aliveCount = alive,
                        totalMortality = mortality,
                        mortalityPercentage = mortalityPct,
                        totalFeed = totalFeed,
                        feedConversion = conversion,
                        avgWeight = avgWeight,
                        totalCost = totalExpenses,
                        costPerChicken = costPerChicken,
                        costPerKilo = costPerKilo,
                        totalSales = totalSales,
                        profit = profit,
                        profitability = profitability
                    )
                }
                _reports.value = reports
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCsvContent(): String {
        return ReportExporter.exportToCsv(_reports.value, app.settingsManager.currency.value)
    }

    fun getPdfContent(): ByteArray {
        return ReportExporter.exportToPdf(_reports.value, app.settingsManager.currency.value)
    }
}
