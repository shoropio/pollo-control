package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.TreatmentDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.TreatmentRepository
import kotlinx.coroutines.flow.Flow

class TreatmentRepositoryImpl(private val dao: TreatmentDao, private val tombstoneDao: SyncTombstoneDao) : TreatmentRepository {
    override fun getByBatch(batchId: Long): Flow<List<TreatmentEntity>> = dao.getByBatch(batchId)
    override fun getAll(): Flow<List<TreatmentEntity>> = dao.getAll()
    override fun getWithdrawalPeriod(today: Long): Flow<List<TreatmentEntity>> = dao.getWithdrawalPeriod(today)
    override suspend fun insert(treatment: TreatmentEntity): Long = dao.insert(treatment)
    override suspend fun update(treatment: TreatmentEntity) = dao.update(treatment)
    override suspend fun delete(treatment: TreatmentEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.TREATMENTS, treatment.id))
        dao.delete(treatment)
    }
}
