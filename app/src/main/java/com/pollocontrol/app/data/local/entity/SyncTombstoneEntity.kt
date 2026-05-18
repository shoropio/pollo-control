package com.pollocontrol.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "sync_tombstones",
    primaryKeys = ["collectionName", "documentId"]
)
data class SyncTombstoneEntity(
    val collectionName: String,
    val documentId: Long,
    val deletedAt: Long = System.currentTimeMillis()
)
