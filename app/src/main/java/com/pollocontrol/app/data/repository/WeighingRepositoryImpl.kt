package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.WeighingDao
import com.pollocontrol.app.data.local.entity.WeighingEntity
import com.pollocontrol.app.domain.repository.WeighingRepository
import kotlinx.coroutines.flow.Flow

class WeighingRepositoryImpl(private val dao: WeighingDao) : WeighingRepository {
    override fun getByBatch(batchId: Long): Flow<List<WeighingEntity>> = dao.getByBatch(batchId)
    override suspend fun getLatest(batchId: Long): WeighingEntity? = dao.getLatest(batchId)
    override fun getLatestFlow(batchId: Long): Flow<WeighingEntity?> = dao.getLatestFlow(batchId)
    override suspend fun insert(weighing: WeighingEntity): Long = dao.insert(weighing)
    override suspend fun update(weighing: WeighingEntity) = dao.update(weighing)
    override suspend fun delete(weighing: WeighingEntity) = dao.delete(weighing)
}
