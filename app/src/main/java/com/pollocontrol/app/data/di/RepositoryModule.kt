package com.pollocontrol.app.data.di

import com.pollocontrol.app.data.local.dao.*
import com.pollocontrol.app.data.repository.*
import com.pollocontrol.app.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBatchRepository(dao: BatchDao, tombstone: SyncTombstoneDao): BatchRepository =
        BatchRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideMortalityRepository(dao: MortalityDao, tombstone: SyncTombstoneDao): MortalityRepository =
        MortalityRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideSupplyRepository(dao: SupplyDao, tombstone: SyncTombstoneDao): SupplyRepository =
        SupplyRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideSupplyMovementRepository(dao: SupplyMovementDao, tombstone: SyncTombstoneDao): SupplyMovementRepository =
        SupplyMovementRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideFeedingRepository(dao: FeedingDao, tombstone: SyncTombstoneDao): FeedingRepository =
        FeedingRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao, tombstone: SyncTombstoneDao): ExpenseRepository =
        ExpenseRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideTreatmentRepository(dao: TreatmentDao, tombstone: SyncTombstoneDao): TreatmentRepository =
        TreatmentRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideWeighingRepository(dao: WeighingDao, tombstone: SyncTombstoneDao): WeighingRepository =
        WeighingRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideSlaughterRepository(dao: SlaughterDao, tombstone: SyncTombstoneDao): SlaughterRepository =
        SlaughterRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideSaleRepository(dao: SaleDao, tombstone: SyncTombstoneDao): SaleRepository =
        SaleRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideClientRepository(dao: ClientDao, tombstone: SyncTombstoneDao): ClientRepository =
        ClientRepositoryImpl(dao, tombstone)

    @Provides
    @Singleton
    fun provideEggProductionRepository(dao: EggProductionDao, tombstone: SyncTombstoneDao): EggProductionRepository =
        EggProductionRepositoryImpl(dao, tombstone)
}
