package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.BatchDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.BatchRepository
import kotlinx.coroutines.flow.Flow

class BatchRepositoryImpl(private val batchDao: BatchDao, private val tombstoneDao: SyncTombstoneDao) : BatchRepository {
    override fun getAll(): Flow<List<BatchEntity>> = batchDao.getAll()
    override suspend fun getById(id: Long): BatchEntity? = batchDao.getById(id)
    override fun getByIdFlow(id: Long): Flow<BatchEntity?> = batchDao.getByIdFlow(id)
    override fun getByEstado(estado: String): Flow<List<BatchEntity>> = batchDao.getByEstado(estado)
    override suspend fun insert(batch: BatchEntity): Long = batchDao.insert(batch)
    override suspend fun update(batch: BatchEntity) = batchDao.update(batch)
    override suspend fun delete(batch: BatchEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.BATCHES, batch.id))
        batchDao.delete(batch)
    }
    override fun getActiveCount(): Flow<Int> = batchDao.getActiveCount()
}
