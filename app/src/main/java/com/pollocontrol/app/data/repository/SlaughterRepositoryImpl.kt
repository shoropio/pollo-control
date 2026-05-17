package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.SlaughterDao
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import com.pollocontrol.app.domain.repository.SlaughterRepository
import kotlinx.coroutines.flow.Flow

class SlaughterRepositoryImpl(private val dao: SlaughterDao) : SlaughterRepository {
    override fun getByBatch(batchId: Long): Flow<List<SlaughterEntity>> = dao.getByBatch(batchId)
    override suspend fun getTotalByBatch(batchId: Long): Int = dao.getTotalByBatch(batchId)
    override suspend fun insert(slaughter: SlaughterEntity): Long = dao.insert(slaughter)
    override suspend fun update(slaughter: SlaughterEntity) = dao.update(slaughter)
    override suspend fun delete(slaughter: SlaughterEntity) = dao.delete(slaughter)
}
