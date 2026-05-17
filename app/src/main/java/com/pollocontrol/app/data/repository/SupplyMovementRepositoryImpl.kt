package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.SupplyMovementDao
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import com.pollocontrol.app.domain.repository.SupplyMovementRepository
import kotlinx.coroutines.flow.Flow

class SupplyMovementRepositoryImpl(private val dao: SupplyMovementDao) : SupplyMovementRepository {
    override fun getBySupply(supplyId: Long): Flow<List<SupplyMovementEntity>> = dao.getBySupply(supplyId)
    override fun getAll(): Flow<List<SupplyMovementEntity>> = dao.getAll()
    override suspend fun insert(movement: SupplyMovementEntity): Long = dao.insert(movement)
    override suspend fun delete(movement: SupplyMovementEntity) = dao.delete(movement)
}
