/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.EggProductionEntity
import kotlinx.coroutines.flow.Flow

interface EggProductionRepository {
    fun getByBatch(loteId: Long): Flow<List<EggProductionEntity>>
    suspend fun getById(id: Long): EggProductionEntity?
    suspend fun getTotalByBatch(loteId: Long): Int
    suspend fun getTotalBetweenDates(loteId: Long, start: Long, end: Long): Int
    fun getAll(): Flow<List<EggProductionEntity>>
    suspend fun insert(entity: EggProductionEntity): Long
    suspend fun update(entity: EggProductionEntity)
    suspend fun delete(entity: EggProductionEntity)
}
