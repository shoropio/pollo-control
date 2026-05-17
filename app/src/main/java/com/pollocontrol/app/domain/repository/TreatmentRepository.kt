package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

interface TreatmentRepository {
    fun getByBatch(batchId: Long): Flow<List<TreatmentEntity>>
    fun getAll(): Flow<List<TreatmentEntity>>
    fun getWithdrawalPeriod(today: Long): Flow<List<TreatmentEntity>>
    suspend fun insert(treatment: TreatmentEntity): Long
    suspend fun update(treatment: TreatmentEntity)
    suspend fun delete(treatment: TreatmentEntity)
}
