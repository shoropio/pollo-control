package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "alimentacion",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loteId")]
)
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val fecha: Long,
    val tipo: String,
    val cantidadKg: Double,
    val cantidadSacos: Int? = null,
    val costo: Double? = null,
    val observaciones: String = ""
)
