/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "produccion_huevos",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("loteId"),
        Index("fecha"),
        Index(value = ["fecha", "loteId"], name = "idx_eggs_fecha_lote")
    ]
)
data class EggProductionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val fecha: Long,
    val cantidadHuevos: Int,
    val pesoPromedio: Double = 0.0,
    val calidadA: Int = 0,
    val calidadB: Int = 0,
    val rechazos: Int = 0,
    val observaciones: String = ""
)
