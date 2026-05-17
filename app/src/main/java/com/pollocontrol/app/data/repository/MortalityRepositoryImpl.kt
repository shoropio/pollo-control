package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.MortalityDao
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.domain.repository.MortalityRepository
import kotlinx.coroutines.flow.Flow

class MortalityRepositoryImpl(private val dao: MortalityDao) : MortalityRepository {
    override fun getByBatch(batchId: Long): Flow<List<MortalityEntity>> = dao.getByBatch(batchId)
    override suspend fun getTotalByBatch(batchId: Long): Int = dao.getTotalByBatch(batchId)
    override fun getTotalByBatchFlow(batchId: Long): Flow<Int> = dao.getTotalByBatchFlow(batchId)
    override suspend fun insert(mortality: MortalityEntity): Long = dao.insert(mortality)
    override suspend fun update(mortality: MortalityEntity) = dao.update(mortality)
    override suspend fun delete(mortality: MortalityEntity) = dao.delete(mortality)
}
