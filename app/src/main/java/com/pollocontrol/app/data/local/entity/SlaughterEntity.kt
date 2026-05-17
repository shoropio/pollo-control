package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "sacrificios",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loteId")]
)
data class SlaughterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val fecha: Long,
    val cantidad: Int,
    val pesoVivoPromedio: Double,
    val pesoCanalPromedio: Double,
    val merma: Double = 0.0,
    val costoSacrificio: Double? = null,
    val observaciones: String = ""
)
