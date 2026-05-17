package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import kotlinx.coroutines.flow.Flow

interface SupplyMovementRepository {
    fun getBySupply(supplyId: Long): Flow<List<SupplyMovementEntity>>
    fun getAll(): Flow<List<SupplyMovementEntity>>
    suspend fun insert(movement: SupplyMovementEntity): Long
    suspend fun delete(movement: SupplyMovementEntity)
}
