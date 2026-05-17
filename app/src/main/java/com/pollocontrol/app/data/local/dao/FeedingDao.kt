package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.FeedingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Query("SELECT * FROM alimentacion WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<FeedingEntity>>

    @Query("SELECT COALESCE(SUM(cantidadKg), 0) FROM alimentacion WHERE loteId = :batchId")
    suspend fun getTotalByBatch(batchId: Long): Double

    @Query("SELECT COALESCE(SUM(cantidadKg), 0) FROM alimentacion WHERE loteId = :batchId AND tipo = :type")
    suspend fun getTotalByBatchAndType(batchId: Long, type: String): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feeding: FeedingEntity): Long

    @Update
    suspend fun update(feeding: FeedingEntity)

    @Delete
    suspend fun delete(feeding: FeedingEntity)
}
