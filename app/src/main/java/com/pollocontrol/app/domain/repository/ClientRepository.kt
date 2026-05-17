package com.pollocontrol.app.domain.repository

import com.pollocontrol.app.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getAll(): Flow<List<ClientEntity>>
    suspend fun getById(id: Long): ClientEntity?
    fun getByIdFlow(id: Long): Flow<ClientEntity?>
    fun search(query: String): Flow<List<ClientEntity>>
    suspend fun insert(client: ClientEntity): Long
    suspend fun update(client: ClientEntity)
    suspend fun delete(client: ClientEntity)
}
