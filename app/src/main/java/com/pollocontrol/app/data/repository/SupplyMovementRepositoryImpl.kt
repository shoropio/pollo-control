package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.SupplyMovementDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.SupplyMovementRepository
import kotlinx.coroutines.flow.Flow

class SupplyMovementRepositoryImpl(private val dao: SupplyMovementDao, private val tombstoneDao: SyncTombstoneDao) : SupplyMovementRepository {
    override fun getBySupply(supplyId: Long): Flow<List<SupplyMovementEntity>> = dao.getBySupply(supplyId)
    override fun getAll(): Flow<List<SupplyMovementEntity>> = dao.getAll()
    override suspend fun insert(movement: SupplyMovementEntity): Long = dao.insert(movement)
    override suspend fun delete(movement: SupplyMovementEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.SUPPLY_MOVEMENTS, movement.id))
        dao.delete(movement)
    }
}
