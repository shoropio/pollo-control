/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.EggProductionDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.EggProductionEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.EggProductionRepository
import kotlinx.coroutines.flow.Flow

class EggProductionRepositoryImpl(
    private val dao: EggProductionDao,
    private val tombstoneDao: SyncTombstoneDao
) : EggProductionRepository {
    override fun getByBatch(loteId: Long): Flow<List<EggProductionEntity>> = dao.getByBatch(loteId)

    override suspend fun getById(id: Long): EggProductionEntity? = dao.getById(id)

    override suspend fun getTotalByBatch(loteId: Long): Int = dao.getTotalByBatch(loteId)

    override suspend fun getTotalBetweenDates(loteId: Long, start: Long, end: Long): Int =
        dao.getTotalBetweenDates(loteId, start, end)

    override fun getAll(): Flow<List<EggProductionEntity>> = dao.getAll()

    override suspend fun insert(entity: EggProductionEntity): Long = dao.insert(entity)

    override suspend fun update(entity: EggProductionEntity) = dao.update(entity)

    override suspend fun delete(entity: EggProductionEntity) {
        dao.delete(entity)
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.EGG_PRODUCTION, entity.id))
    }
}
