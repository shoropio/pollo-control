/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.QuailBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuailBatchDao {
    @Query("SELECT * FROM lotes_codornices ORDER BY fechaIngreso DESC")
    fun getAll(): Flow<List<QuailBatchEntity>>

    @Query("SELECT * FROM lotes_codornices WHERE estado = :estado ORDER BY fechaIngreso DESC")
    fun getByEstado(estado: String): Flow<List<QuailBatchEntity>>

    @Query("SELECT * FROM lotes_codornices WHERE id = :id")
    suspend fun getById(id: Long): QuailBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuailBatchEntity): Long

    @Update
    suspend fun update(entity: QuailBatchEntity)

    @Delete
    suspend fun delete(entity: QuailBatchEntity)

    @Query("DELETE FROM lotes_codornices WHERE id = :id")
    suspend fun deleteById(id: Long)
}
