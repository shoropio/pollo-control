package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SlaughterDao {
    @Query("SELECT * FROM sacrificios WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<SlaughterEntity>>

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM sacrificios WHERE loteId = :batchId")
    suspend fun getTotalByBatch(batchId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slaughter: SlaughterEntity): Long

    @Update
    suspend fun update(slaughter: SlaughterEntity)

    @Delete
    suspend fun delete(slaughter: SlaughterEntity)
}
