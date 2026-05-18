package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {
    @Query("SELECT * FROM tratamientos WHERE loteId = :batchId ORDER BY fechaAplicacion DESC")
    fun getByBatch(batchId: Long): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM tratamientos ORDER BY fechaAplicacion DESC")
    fun getAll(): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM tratamientos WHERE periodoRetiro IS NOT NULL AND fechaAplicacion + (periodoRetiro * 86400000) > :today")
    fun getWithdrawalPeriod(today: Long): Flow<List<TreatmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(treatment: TreatmentEntity): Long

    @Update
    suspend fun update(treatment: TreatmentEntity)

    @Delete
    suspend fun delete(treatment: TreatmentEntity)

    @Query("DELETE FROM tratamientos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
