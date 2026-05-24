/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app

import android.app.Application
import com.pollocontrol.app.data.auth.AuthManager
import com.pollocontrol.app.data.cache.AppDataCache
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.repository.*
import com.pollocontrol.app.data.settings.SettingsManager
import com.pollocontrol.app.data.sync.FirebaseSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PolloControlApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val authManager by lazy { AuthManager(this) }
    val settingsManager by lazy { SettingsManager(this) }

    val database by lazy { PolloControlDatabase.getDatabase(this) }
    val dataCache by lazy { AppDataCache(database, applicationScope) }

    val batchRepository by lazy { BatchRepositoryImpl(database.batchDao(), database.syncTombstoneDao()) }
    val mortalityRepository by lazy { MortalityRepositoryImpl(database.mortalityDao(), database.syncTombstoneDao()) }
    val supplyRepository by lazy { SupplyRepositoryImpl(database.supplyDao(), database.syncTombstoneDao()) }
    val feedingRepository by lazy { FeedingRepositoryImpl(database.feedingDao(), database.syncTombstoneDao()) }
    val expenseRepository by lazy { ExpenseRepositoryImpl(database.expenseDao(), database.syncTombstoneDao()) }
    val treatmentRepository by lazy { TreatmentRepositoryImpl(database.treatmentDao(), database.syncTombstoneDao()) }
    val weighingRepository by lazy { WeighingRepositoryImpl(database.weighingDao(), database.syncTombstoneDao()) }
    val slaughterRepository by lazy { SlaughterRepositoryImpl(database.slaughterDao(), database.syncTombstoneDao()) }
    val saleRepository by lazy { SaleRepositoryImpl(database.saleDao(), database.syncTombstoneDao()) }
    val clientRepository by lazy { ClientRepositoryImpl(database.clientDao(), database.syncTombstoneDao()) }
    val supplyMovementRepository by lazy { SupplyMovementRepositoryImpl(database.supplyMovementDao(), database.syncTombstoneDao()) }

    // ✅ Repositories para Codornices y Huevos
    val quailBatchRepository by lazy { QuailBatchRepositoryImpl(database.quailBatchDao(), database.syncTombstoneDao()) }
    val eggProductionRepository by lazy { EggProductionRepositoryImpl(database.eggProductionDao(), database.syncTombstoneDao()) }

    val firebaseSyncManager by lazy { FirebaseSyncManager(this) }
}
