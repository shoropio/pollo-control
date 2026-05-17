package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.MortalityEntity
import kotlinx.coroutines.flow.Flow

interface MortalityRepository {
    fun getByBatch(batchId: Long): Flow<List<MortalityEntity>>
    suspend fun getTotalByBatch(batchId: Long): Int
    fun getTotalByBatchFlow(batchId: Long): Flow<Int>
    suspend fun insert(mortality: MortalityEntity): Long
    suspend fun update(mortality: MortalityEntity)
    suspend fun delete(mortality: MortalityEntity)
}
