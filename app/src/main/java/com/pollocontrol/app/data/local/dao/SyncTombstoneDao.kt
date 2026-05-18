package com.pollocontrol.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pollocontrol.app.data.local.entity.SyncTombstoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncTombstoneDao {
    @Query("SELECT * FROM sync_tombstones ORDER BY deletedAt ASC")
    fun getAll(): Flow<List<SyncTombstoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tombstone: SyncTombstoneEntity)

    @Delete
    suspend fun delete(tombstone: SyncTombstoneEntity)
}
