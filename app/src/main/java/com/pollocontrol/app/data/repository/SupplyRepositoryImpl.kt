package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.SupplyDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.SupplyRepository
import kotlinx.coroutines.flow.Flow

class SupplyRepositoryImpl(private val dao: SupplyDao, private val tombstoneDao: SyncTombstoneDao) : SupplyRepository {
    override fun getAll(): Flow<List<SupplyEntity>> = dao.getAll()
    override suspend fun getById(id: Long): SupplyEntity? = dao.getById(id)
    override fun getLowStock(): Flow<List<SupplyEntity>> = dao.getLowStock()
    override fun getByType(type: String): Flow<List<SupplyEntity>> = dao.getByType(type)
    override suspend fun insert(supply: SupplyEntity): Long = dao.insert(supply)
    override suspend fun update(supply: SupplyEntity) = dao.update(supply)
    override suspend fun delete(supply: SupplyEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.SUPPLIES, supply.id))
        dao.delete(supply)
    }
    override suspend fun addStock(id: Long, amount: Double) = dao.addStock(id, amount)
    override suspend fun removeStock(id: Long, amount: Double) = dao.removeStock(id, amount)
}
