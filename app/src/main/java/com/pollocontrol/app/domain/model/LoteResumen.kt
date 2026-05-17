package com.pollocontrol.app.domain.model

data class LoteResumen(
    val id: Long,
    val nombre: String,
    val fechaIngreso: Long,
    val cantidadInicial: Int,
    val raza: String,
    val galpon: String,
    val estado: String,
    val pollosVivos: Int = 0,
    val mortalidadTotal: Int = 0,
    val edad: Int = 0,
    val costoTotal: Double = 0.0,
    val costoPorPollo: Double = 0.0,
    val gastoAlimento: Double = 0.0
)
