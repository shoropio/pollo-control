package com.pollocontrol.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pollocontrol.app.data.local.converter.DateConverter
import com.pollocontrol.app.data.local.dao.*
import com.pollocontrol.app.data.local.entity.*

@Database(
    entities = [
        BatchEntity::class,
        MortalityEntity::class,
        SupplyEntity::class,
        SupplyMovementEntity::class,
        FeedingEntity::class,
        ExpenseEntity::class,
        TreatmentEntity::class,
        WeighingEntity::class,
        SlaughterEntity::class,
        SaleEntity::class,
        ClientEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class PolloControlDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun mortalityDao(): MortalityDao
    abstract fun supplyDao(): SupplyDao
    abstract fun supplyMovementDao(): SupplyMovementDao
    abstract fun feedingDao(): FeedingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun weighingDao(): WeighingDao
    abstract fun slaughterDao(): SlaughterDao
    abstract fun saleDao(): SaleDao
    abstract fun clientDao(): ClientDao

    companion object {
        @Volatile
        private var INSTANCE: PolloControlDatabase? = null

        fun getDatabase(context: Context): PolloControlDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PolloControlDatabase::class.java,
                    "pollocontrol_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
