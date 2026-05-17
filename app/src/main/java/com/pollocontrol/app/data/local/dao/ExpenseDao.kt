package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE loteId IS NULL ORDER BY fecha DESC")
    fun getGeneral(): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE loteId = :batchId")
    suspend fun getTotalByBatch(batchId: Long): Double

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE loteId IS NULL")
    suspend fun getTotalGeneral(): Double

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos")
    suspend fun getTotal(): Double

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE fecha BETWEEN :start AND :end")
    suspend fun getTotalBetweenDates(start: Long, end: Long): Double

    @Query("SELECT * FROM gastos WHERE fecha BETWEEN :start AND :end ORDER BY fecha DESC")
    fun getBetweenDatesFlow(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE tipo = :type AND loteId = :batchId")
    suspend fun getTotalByTypeAndBatch(type: String, batchId: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
