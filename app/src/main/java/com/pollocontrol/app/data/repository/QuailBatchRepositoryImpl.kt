/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.QuailBatchDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.QuailBatchEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.QuailBatchRepository
import kotlinx.coroutines.flow.Flow

class QuailBatchRepositoryImpl(
    private val dao: QuailBatchDao,
    private val tombstoneDao: SyncTombstoneDao
) : QuailBatchRepository {
    override fun getAll(): Flow<List<QuailBatchEntity>> = dao.getAll()

    override fun getByEstado(estado: String): Flow<List<QuailBatchEntity>> = dao.getByEstado(estado)

    override suspend fun getById(id: Long): QuailBatchEntity? = dao.getById(id)

    override suspend fun insert(entity: QuailBatchEntity): Long = dao.insert(entity)

    override suspend fun update(entity: QuailBatchEntity) = dao.update(entity)

    override suspend fun delete(entity: QuailBatchEntity) {
        dao.delete(entity)
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.QUAIL_BATCHES, entity.id))
    }
}
