package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAll(): Flow<List<SaleEntity>>
    fun getByBatch(batchId: Long): Flow<List<SaleEntity>>
    fun getByClient(clientId: Long): Flow<List<SaleEntity>>
    suspend fun getTotalBetweenDates(start: Long, end: Long): Double
    suspend fun getTotalSince(since: Long): Double
    suspend fun getTotal(): Double
    fun getPending(): Flow<List<SaleEntity>>
    suspend fun insert(sale: SaleEntity): Long
    suspend fun update(sale: SaleEntity)
    suspend fun delete(sale: SaleEntity)
}
