package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.SupplyEntity
import kotlinx.coroutines.flow.Flow

interface SupplyRepository {
    fun getAll(): Flow<List<SupplyEntity>>
    suspend fun getById(id: Long): SupplyEntity?
    fun getLowStock(): Flow<List<SupplyEntity>>
    fun getByType(type: String): Flow<List<SupplyEntity>>
    suspend fun insert(supply: SupplyEntity): Long
    suspend fun update(supply: SupplyEntity)
    suspend fun delete(supply: SupplyEntity)
    suspend fun addStock(id: Long, amount: Double)
    suspend fun removeStock(id: Long, amount: Double)
}
