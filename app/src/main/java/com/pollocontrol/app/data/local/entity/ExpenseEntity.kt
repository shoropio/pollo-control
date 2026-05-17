package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "gastos",
    foreignKeys = [ForeignKey(
        entity = BatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["loteId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("loteId")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long? = null,
    val tipo: String,
    val descripcion: String,
    val monto: Double,
    val fecha: Long,
    val observaciones: String = ""
)
