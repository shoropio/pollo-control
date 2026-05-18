package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.FeedingDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.FeedingRepository
import kotlinx.coroutines.flow.Flow

class FeedingRepositoryImpl(private val dao: FeedingDao, private val tombstoneDao: SyncTombstoneDao) : FeedingRepository {
    override fun getByBatch(batchId: Long): Flow<List<FeedingEntity>> = dao.getByBatch(batchId)
    override suspend fun getTotalByBatch(batchId: Long): Double = dao.getTotalByBatch(batchId)
    override suspend fun getTotalByBatchAndType(batchId: Long, type: String): Double = dao.getTotalByBatchAndType(batchId, type)
    override suspend fun insert(feeding: FeedingEntity): Long = dao.insert(feeding)
    override suspend fun update(feeding: FeedingEntity) = dao.update(feeding)
    override suspend fun delete(feeding: FeedingEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.FEEDING, feeding.id))
        dao.delete(feeding)
    }
}
