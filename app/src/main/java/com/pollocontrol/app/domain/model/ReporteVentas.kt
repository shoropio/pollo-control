package com.pollocontrol.app.domain.model

data class ReporteVentas(
    val periodo: String,
    val totalVentas: Double,
    val cantidadPollos: Int,
    val ticketPromedio: Double
)
