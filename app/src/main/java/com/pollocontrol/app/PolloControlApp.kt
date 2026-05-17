package com.pollocontrol.app

import android.app.Application
import com.pollocontrol.app.data.auth.AuthManager
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.repository.*
import com.pollocontrol.app.data.sync.FirebaseSyncManager

class PolloControlApp : Application() {

    val authManager by lazy { AuthManager(this) }

    val database by lazy { PolloControlDatabase.getDatabase(this) }

    val batchRepository by lazy { BatchRepositoryImpl(database.batchDao()) }
    val mortalityRepository by lazy { MortalityRepositoryImpl(database.mortalityDao()) }
    val supplyRepository by lazy { SupplyRepositoryImpl(database.supplyDao()) }
    val feedingRepository by lazy { FeedingRepositoryImpl(database.feedingDao()) }
    val expenseRepository by lazy { ExpenseRepositoryImpl(database.expenseDao()) }
    val treatmentRepository by lazy { TreatmentRepositoryImpl(database.treatmentDao()) }
    val weighingRepository by lazy { WeighingRepositoryImpl(database.weighingDao()) }
    val slaughterRepository by lazy { SlaughterRepositoryImpl(database.slaughterDao()) }
    val saleRepository by lazy { SaleRepositoryImpl(database.saleDao()) }
    val clientRepository by lazy { ClientRepositoryImpl(database.clientDao()) }
    val supplyMovementRepository by lazy { SupplyMovementRepositoryImpl(database.supplyMovementDao()) }

    val firebaseSyncManager by lazy { FirebaseSyncManager(this) }
}
