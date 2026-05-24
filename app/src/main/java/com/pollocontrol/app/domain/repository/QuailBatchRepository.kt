/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.QuailBatchEntity
import kotlinx.coroutines.flow.Flow

interface QuailBatchRepository {
    fun getAll(): Flow<List<QuailBatchEntity>>
    fun getByEstado(estado: String): Flow<List<QuailBatchEntity>>
    suspend fun getById(id: Long): QuailBatchEntity?
    suspend fun insert(entity: QuailBatchEntity): Long
    suspend fun update(entity: QuailBatchEntity)
    suspend fun delete(entity: QuailBatchEntity)
}
