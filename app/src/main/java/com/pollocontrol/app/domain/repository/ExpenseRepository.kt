package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAll(): Flow<List<ExpenseEntity>>
    fun getByBatch(batchId: Long): Flow<List<ExpenseEntity>>
    fun getGeneral(): Flow<List<ExpenseEntity>>
    suspend fun getTotalByBatch(batchId: Long): Double
    suspend fun getTotalGeneral(): Double
    suspend fun getTotal(): Double
    suspend fun getTotalBetweenDates(start: Long, end: Long): Double
    fun getBetweenDatesFlow(start: Long, end: Long): Flow<List<ExpenseEntity>>
    suspend fun getTotalByTypeAndBatch(type: String, batchId: Long): Double
    suspend fun insert(expense: ExpenseEntity): Long
    suspend fun update(expense: ExpenseEntity)
    suspend fun delete(expense: ExpenseEntity)
}
