package com.pollocontrol.app.data.di

import android.content.Context
import androidx.room.Room
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PolloControlDatabase {
        return PolloControlDatabase.getDatabase(context)
    }

    @Provides fun provideBatchDao(db: PolloControlDatabase): BatchDao = db.batchDao()
    @Provides fun provideMortalityDao(db: PolloControlDatabase): MortalityDao = db.mortalityDao()
    @Provides fun provideSupplyDao(db: PolloControlDatabase): SupplyDao = db.supplyDao()
    @Provides fun provideSupplyMovementDao(db: PolloControlDatabase): SupplyMovementDao = db.supplyMovementDao()
    @Provides fun provideFeedingDao(db: PolloControlDatabase): FeedingDao = db.feedingDao()
    @Provides fun provideExpenseDao(db: PolloControlDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideTreatmentDao(db: PolloControlDatabase): TreatmentDao = db.treatmentDao()
    @Provides fun provideWeighingDao(db: PolloControlDatabase): WeighingDao = db.weighingDao()
    @Provides fun provideSlaughterDao(db: PolloControlDatabase): SlaughterDao = db.slaughterDao()
    @Provides fun provideSaleDao(db: PolloControlDatabase): SaleDao = db.saleDao()
    @Provides fun provideClientDao(db: PolloControlDatabase): ClientDao = db.clientDao()
    @Provides fun provideSyncTombstoneDao(db: PolloControlDatabase): SyncTombstoneDao = db.syncTombstoneDao()
    @Provides fun provideEggProductionDao(db: PolloControlDatabase): EggProductionDao = db.eggProductionDao()
}
