package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "movimientos_insumo",
    foreignKeys = [ForeignKey(
        entity = SupplyEntity::class,
        parentColumns = ["id"],
        childColumns = ["insumoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("insumoId")]
)
data class SupplyMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val insumoId: Long,
    val tipo: String,
    val cantidad: Double,
    val fecha: Long,
    val costo: Double? = null,
    val loteId: Long? = null,
    val observaciones: String = ""
)
