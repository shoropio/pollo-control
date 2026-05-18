package com.pollocontrol.app.data.repository

import com.pollocontrol.app.data.local.dao.ClientDao
import com.pollocontrol.app.data.local.dao.SyncTombstoneDao
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import com.pollocontrol.app.data.sync.SyncCollections
import com.pollocontrol.app.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow

class ClientRepositoryImpl(private val dao: ClientDao, private val tombstoneDao: SyncTombstoneDao) : ClientRepository {
    override fun getAll(): Flow<List<ClientEntity>> = dao.getAll()
    override suspend fun getById(id: Long): ClientEntity? = dao.getById(id)
    override fun getByIdFlow(id: Long): Flow<ClientEntity?> = dao.getByIdFlow(id)
    override fun search(query: String): Flow<List<ClientEntity>> = dao.search(query)
    override suspend fun insert(client: ClientEntity): Long = dao.insert(client)
    override suspend fun update(client: ClientEntity) = dao.update(client)
    override suspend fun delete(client: ClientEntity) {
        tombstoneDao.insert(SyncTombstoneEntity(SyncCollections.CLIENTS, client.id))
        dao.delete(client)
    }
}
