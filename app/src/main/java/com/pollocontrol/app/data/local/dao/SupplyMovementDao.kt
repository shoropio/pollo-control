package com.pollocontrol.app.data.local.dao

import androidx.room.*
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplyMovementDao {
    @Query("SELECT * FROM movimientos_insumo WHERE insumoId = :supplyId ORDER BY fecha DESC")
    fun getBySupply(supplyId: Long): Flow<List<SupplyMovementEntity>>

    @Query("SELECT * FROM movimientos_insumo ORDER BY fecha DESC")
    fun getAll(): Flow<List<SupplyMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: SupplyMovementEntity): Long

    @Delete
    suspend fun delete(movement: SupplyMovementEntity)

    @Query("DELETE FROM movimientos_insumo WHERE id = :id")
    suspend fun deleteById(id: Long)
}
