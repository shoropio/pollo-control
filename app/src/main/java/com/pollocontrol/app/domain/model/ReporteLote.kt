package com.pollocontrol.app.domain.model

data class ReporteLote(
    val loteNombre: String,
    val raza: String,
    val fechaIngreso: String,
    val edad: Int,
    val cantidadInicial: Int,
    val pollosVivos: Int,
    val mortalidadTotal: Int,
    val porcentajeMortalidad: Double,
    val totalAlimento: Double,
    val conversionAlimenticia: Double,
    val pesoPromedio: Double,
    val costoTotal: Double,
    val costoPorPollo: Double,
    val costoPorKilo: Double,
    val totalVentas: Double,
    val utilidad: Double,
    val rentabilidad: Double
)
