package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pesajes",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loteId")]
)
data class WeighingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val fecha: Long,
    val edadLote: Int,
    val cantidadAves: Int,
    val pesoPromedio: Double,
    val observaciones: String = ""
)
