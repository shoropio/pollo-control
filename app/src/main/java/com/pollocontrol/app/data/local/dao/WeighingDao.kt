package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.WeighingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeighingDao {
    @Query("SELECT * FROM pesajes WHERE loteId = :batchId ORDER BY fecha DESC")
    fun getByBatch(batchId: Long): Flow<List<WeighingEntity>>

    @Query("SELECT * FROM pesajes WHERE loteId = :batchId ORDER BY fecha DESC LIMIT 1")
    suspend fun getLatest(batchId: Long): WeighingEntity?

    @Query("SELECT * FROM pesajes WHERE loteId = :batchId ORDER BY fecha DESC LIMIT 1")
    fun getLatestFlow(batchId: Long): Flow<WeighingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weighing: WeighingEntity): Long

    @Update
    suspend fun update(weighing: WeighingEntity)

    @Delete
    suspend fun delete(weighing: WeighingEntity)
}
