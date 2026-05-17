package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class SupplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: String,
    val stockActual: Double = 0.0,
    val stockMinimo: Double = 0.0,
    val unidad: String = "",
    val observaciones: String = ""
)
