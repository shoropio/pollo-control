package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT * FROM clientes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :query || '%' OR telefono LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}
