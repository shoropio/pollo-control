package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "ventas",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("loteId"), Index("clienteId")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long? = null,
    val clienteId: Long? = null,
    val fecha: Long,
    val tipo: String,
    val modalidad: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val total: Double,
    val metodoPago: String = "EFECTIVO",
    val estadoPago: String = "PAGADO",
    val observaciones: String = ""
)
