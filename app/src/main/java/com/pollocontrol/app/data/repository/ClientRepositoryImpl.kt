package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.ClientDao
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow

class ClientRepositoryImpl(private val dao: ClientDao) : ClientRepository {
    override fun getAll(): Flow<List<ClientEntity>> = dao.getAll()
    override suspend fun getById(id: Long): ClientEntity? = dao.getById(id)
    override fun getByIdFlow(id: Long): Flow<ClientEntity?> = dao.getByIdFlow(id)
    override fun search(query: String): Flow<List<ClientEntity>> = dao.search(query)
    override suspend fun insert(client: ClientEntity): Long = dao.insert(client)
    override suspend fun update(client: ClientEntity) = dao.update(client)
    override suspend fun delete(client: ClientEntity) = dao.delete(client)
}
