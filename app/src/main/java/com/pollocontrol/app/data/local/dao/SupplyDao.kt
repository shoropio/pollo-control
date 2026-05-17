package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.SupplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplyDao {
    @Query("SELECT * FROM insumos ORDER BY nombre ASC")
    fun getAll(): Flow<List<SupplyEntity>>

    @Query("SELECT * FROM insumos WHERE id = :id")
    suspend fun getById(id: Long): SupplyEntity?

    @Query("SELECT * FROM insumos WHERE stockActual <= stockMinimo")
    fun getLowStock(): Flow<List<SupplyEntity>>

    @Query("SELECT * FROM insumos WHERE tipo = :type ORDER BY nombre ASC")
    fun getByType(type: String): Flow<List<SupplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supply: SupplyEntity): Long

    @Update
    suspend fun update(supply: SupplyEntity)

    @Delete
    suspend fun delete(supply: SupplyEntity)

    @Query("UPDATE insumos SET stockActual = stockActual + :amount WHERE id = :id")
    suspend fun addStock(id: Long, amount: Double)

    @Query("UPDATE insumos SET stockActual = stockActual - :amount WHERE id = :id")
    suspend fun removeStock(id: Long, amount: Double)
}
