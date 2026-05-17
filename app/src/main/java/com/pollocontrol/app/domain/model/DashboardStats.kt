package com.pollocontrol.app.domain.model

data class DashboardStats(
    val activeLotes: Int = 0,
    val totalLiveChickens: Int = 0,
    val totalMortality: Int = 0,
    val totalExpenses: Double = 0.0,
    val totalSales: Double = 0.0,
    val costPerChicken: Double = 0.0,
    val costPerKilo: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val salesToday: Double = 0.0,
    val salesWeek: Double = 0.0,
    val salesMonth: Double = 0.0,
    val lowStockItems: Int = 0,
    val pendingPayments: Int = 0,
    val mortalityRate: Double = 0.0,
    val feedConversion: Double = 0.0,
    val averageWeight: Double = 0.0,
    val totalFeedConsumed: Double = 0.0
)
