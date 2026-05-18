package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM lotes ORDER BY fechaIngreso DESC")
    fun getAll(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM lotes WHERE id = :id")
    suspend fun getById(id: Long): BatchEntity?

    @Query("SELECT * FROM lotes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<BatchEntity?>

    @Query("SELECT * FROM lotes WHERE estado = :estado ORDER BY fechaIngreso DESC")
    fun getByEstado(estado: String): Flow<List<BatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: BatchEntity): Long

    @Update
    suspend fun update(batch: BatchEntity)

    @Delete
    suspend fun delete(batch: BatchEntity)

    @Query("DELETE FROM lotes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM lotes WHERE estado = 'ACTIVO'")
    fun getActiveCount(): Flow<Int>
}
