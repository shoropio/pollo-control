/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "lotes_codornices",
    indices = [
        Index("estado"),
        Index("fechaIngreso"),
        Index(value = ["estado", "fechaIngreso"], name = "idx_quail_estado_fecha")
    ]
)
data class QuailBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String = "",
    val fechaIngreso: Long = 0L,
    val cantidadInicial: Int = 0,
    val precioUnitario: Double = 0.0,
    val raza: String = "",
    val galpon: String = "",
    val proveedor: String = "",
    val estado: String = "ACTIVO",
    val observaciones: String = ""
)
