package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.FeedingDao
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.domain.repository.FeedingRepository
import kotlinx.coroutines.flow.Flow

class FeedingRepositoryImpl(private val dao: FeedingDao) : FeedingRepository {
    override fun getByBatch(batchId: Long): Flow<List<FeedingEntity>> = dao.getByBatch(batchId)
    override suspend fun getTotalByBatch(batchId: Long): Double = dao.getTotalByBatch(batchId)
    override suspend fun getTotalByBatchAndType(batchId: Long, type: String): Double = dao.getTotalByBatchAndType(batchId, type)
    override suspend fun insert(feeding: FeedingEntity): Long = dao.insert(feeding)
    override suspend fun update(feeding: FeedingEntity) = dao.update(feeding)
    override suspend fun delete(feeding: FeedingEntity) = dao.delete(feeding)
}
