package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM ventas WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM ventas WHERE clienteId = :clientId ORDER BY fecha DESC")
    fun getByClient(clientId: Long): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha BETWEEN :start AND :end")
    suspend fun getTotalBetweenDates(start: Long, end: Long): Double

    @Query("SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha >= :since")
    suspend fun getTotalSince(since: Long): Double

    @Query("SELECT COALESCE(SUM(total), 0) FROM ventas")
    suspend fun getTotal(): Double

    @Query("SELECT * FROM ventas WHERE estadoPago = 'PENDIENTE' OR estadoPago = 'CREDITO' ORDER BY fecha DESC")
    fun getPending(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity): Long

    @Update
    suspend fun update(sale: SaleEntity)

    @Delete
    suspend fun delete(sale: SaleEntity)

    @Query("DELETE FROM ventas WHERE id = :id")
    suspend fun deleteById(id: Long)
}
