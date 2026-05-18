package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.ExpenseDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(private val dao: ExpenseDao, private val tombstoneDao: SyncTombstoneDao) : ExpenseRepository {
    override fun getAll(): Flow<List<ExpenseEntity>> = dao.getAll()
    override fun getByBatch(batchId: Long): Flow<List<ExpenseEntity>> = dao.getByBatch(batchId)
    override fun getGeneral(): Flow<List<ExpenseEntity>> = dao.getGeneral()
    override suspend fun getTotalByBatch(batchId: Long): Double = dao.getTotalByBatch(batchId)
    override suspend fun getTotalGeneral(): Double = dao.getTotalGeneral()
    override suspend fun getTotal(): Double = dao.getTotal()
    override suspend fun getTotalBetweenDates(start: Long, end: Long): Double = dao.getTotalBetweenDates(start, end)
    override fun getBetweenDatesFlow(start: Long, end: Long): Flow<List<ExpenseEntity>> = dao.getBetweenDatesFlow(start, end)
    override suspend fun getTotalByTypeAndBatch(type: String, batchId: Long): Double = dao.getTotalByTypeAndBatch(type, batchId)
    override suspend fun insert(expense: ExpenseEntity): Long = dao.insert(expense)
    override suspend fun update(expense: ExpenseEntity) = dao.update(expense)
    override suspend fun delete(expense: ExpenseEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.EXPENSES, expense.id))
        dao.delete(expense)
    }
}
