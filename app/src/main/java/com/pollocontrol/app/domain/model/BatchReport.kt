package com.pollocontrol.app.domain.model

data class BatchReport(
    val batchName: String,
    val breed: String,
    val entryDate: String,
    val age: Int,
    val initialCount: Int,
    val aliveCount: Int,
    val totalMortality: Int,
    val mortalityPercentage: Double,
    val totalFeed: Double,
    val feedConversion: Double,
    val avgWeight: Double,
    val totalCost: Double,
    val costPerChicken: Double,
    val costPerKilo: Double,
    val totalSales: Double,
    val profit: Double,
    val profitability: Double
)
