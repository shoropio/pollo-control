package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tratamientos",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loteId")]
)
data class TreatmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val tipo: String,
    val nombre: String,
    val fechaAplicacion: Long,
    val dosis: String = "",
    val responsable: String = "",
    val costo: Double? = null,
    val periodoRetiro: Int? = null,
    val observaciones: String = ""
)
