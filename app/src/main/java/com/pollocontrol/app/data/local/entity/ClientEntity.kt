package com.pollocontrol.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val telefono: String = "",
    val direccion: String = "",
    val saldoPendiente: Double = 0.0,
    val observaciones: String = ""
)
