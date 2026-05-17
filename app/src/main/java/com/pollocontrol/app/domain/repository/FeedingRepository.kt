package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.FeedingEntity
import kotlinx.coroutines.flow.Flow

interface FeedingRepository {
    fun getByBatch(batchId: Long): Flow<List<FeedingEntity>>
    suspend fun getTotalByBatch(batchId: Long): Double
    suspend fun getTotalByBatchAndType(batchId: Long, type: String): Double
    suspend fun insert(feeding: FeedingEntity): Long
    suspend fun update(feeding: FeedingEntity)
    suspend fun delete(feeding: FeedingEntity)
}
