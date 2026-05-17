package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    fun getAll(): Flow<List<BatchEntity>>
    suspend fun getById(id: Long): BatchEntity?
    fun getByIdFlow(id: Long): Flow<BatchEntity?>
    fun getByEstado(estado: String): Flow<List<BatchEntity>>
    suspend fun insert(batch: BatchEntity): Long
    suspend fun update(batch: BatchEntity)
    suspend fun delete(batch: BatchEntity)
    fun getActiveCount(): Flow<Int>
}
