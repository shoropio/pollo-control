package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.SaleDao
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class SaleRepositoryImpl(private val dao: SaleDao) : SaleRepository {
    override fun getAll(): Flow<List<SaleEntity>> = dao.getAll()
    override fun getByBatch(batchId: Long): Flow<List<SaleEntity>> = dao.getByBatch(batchId)
    override fun getByClient(clientId: Long): Flow<List<SaleEntity>> = dao.getByClient(clientId)
    override suspend fun getTotalBetweenDates(start: Long, end: Long): Double = dao.getTotalBetweenDates(start, end)
    override suspend fun getTotalSince(since: Long): Double = dao.getTotalSince(since)
    override suspend fun getTotal(): Double = dao.getTotal()
    override fun getPending(): Flow<List<SaleEntity>> = dao.getPending()
    override suspend fun insert(sale: SaleEntity): Long = dao.insert(sale)
    override suspend fun update(sale: SaleEntity) = dao.update(sale)
    override suspend fun delete(sale: SaleEntity) = dao.delete(sale)
}
