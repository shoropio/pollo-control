package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotes")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val fechaIngreso: Long = 0L,
    val cantidadInicial: Int = 0,
    val precioPorPollito: Double = 0.0,
    val raza: String = "",
    val galpon: String = "",
    val proveedor: String = "",
    val estado: String = "ACTIVO",
    val observaciones: String = ""
)
