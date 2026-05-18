package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.MortalityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MortalityDao {
    @Query("SELECT * FROM mortalidad ORDER BY fecha DESC")
    fun getAll(): Flow<List<MortalityEntity>>

    @Query("SELECT * FROM mortalidad WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<MortalityEntity>>

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM mortalidad WHERE loteId = :batchId")
    suspend fun getTotalByBatch(batchId: Long): Int

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM mortalidad WHERE loteId = :batchId")
    fun getTotalByBatchFlow(batchId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mortality: MortalityEntity): Long

    @Update
    suspend fun update(mortality: MortalityEntity)

    @Delete
    suspend fun delete(mortality: MortalityEntity)

    @Query("DELETE FROM mortalidad WHERE id = :id")
    suspend fun deleteById(id: Long)
}
