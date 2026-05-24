/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.EggProductionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EggProductionDao {
    @Query("SELECT * FROM produccion_huevos WHERE loteId = :loteId ORDER BY fecha DESC")
    fun getByBatch(loteId: Long): Flow<List<EggProductionEntity>>

    @Query("SELECT * FROM produccion_huevos WHERE id = :id")
    suspend fun getById(id: Long): EggProductionEntity?

    @Query("SELECT SUM(cantidadHuevos) FROM produccion_huevos WHERE loteId = :loteId")
    suspend fun getTotalByBatch(loteId: Long): Int

    @Query("SELECT SUM(cantidadHuevos) FROM produccion_huevos WHERE loteId = :loteId AND fecha BETWEEN :start AND :end")
    suspend fun getTotalBetweenDates(loteId: Long, start: Long, end: Long): Int

    @Query("SELECT * FROM produccion_huevos ORDER BY fecha DESC")
    fun getAll(): Flow<List<EggProductionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EggProductionEntity): Long

    @Update
    suspend fun update(entity: EggProductionEntity)

    @Delete
    suspend fun delete(entity: EggProductionEntity)

    @Query("DELETE FROM produccion_huevos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
