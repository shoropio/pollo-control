package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.TreatmentDao
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import com.pollocontrol.app.domain.repository.TreatmentRepository
import kotlinx.coroutines.flow.Flow

class TreatmentRepositoryImpl(private val dao: TreatmentDao) : TreatmentRepository {
    override fun getByBatch(batchId: Long): Flow<List<TreatmentEntity>> = dao.getByBatch(batchId)
    override fun getAll(): Flow<List<TreatmentEntity>> = dao.getAll()
    override fun getWithdrawalPeriod(today: Long): Flow<List<TreatmentEntity>> = dao.getWithdrawalPeriod(today)
    override suspend fun insert(treatment: TreatmentEntity): Long = dao.insert(treatment)
    override suspend fun update(treatment: TreatmentEntity) = dao.update(treatment)
    override suspend fun delete(treatment: TreatmentEntity) = dao.delete(treatment)
}
