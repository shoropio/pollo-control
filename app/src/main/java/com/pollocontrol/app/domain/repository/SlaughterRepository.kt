package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.SlaughterEntity
import kotlinx.coroutines.flow.Flow

interface SlaughterRepository {
    fun getByBatch(batchId: Long): Flow<List<SlaughterEntity>>
    suspend fun getTotalByBatch(batchId: Long): Int
    suspend fun insert(slaughter: SlaughterEntity): Long
    suspend fun update(slaughter: SlaughterEntity)
    suspend fun delete(slaughter: SlaughterEntity)
}
