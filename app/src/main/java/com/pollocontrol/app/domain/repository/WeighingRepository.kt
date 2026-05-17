package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.WeighingEntity
import kotlinx.coroutines.flow.Flow

interface WeighingRepository {
    fun getByBatch(batchId: Long): Flow<List<WeighingEntity>>
    suspend fun getLatest(batchId: Long): WeighingEntity?
    fun getLatestFlow(batchId: Long): Flow<WeighingEntity?>
    suspend fun insert(weighing: WeighingEntity): Long
    suspend fun update(weighing: WeighingEntity)
    suspend fun delete(weighing: WeighingEntity)
}
