package com.pollocontrol.app.domain.model

data class SalesReport(
    val period: String,
    val totalSales: Double,
    val chickenCount: Int,
    val averageTicket: Double
)
