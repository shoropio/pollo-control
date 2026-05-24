package com.pollocontrol.app.data.cache

import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppDataCache(
    database: PolloControlDatabase,
    scope: CoroutineScope
) {
    val batches: StateFlow<List<BatchEntity>> =
        database.batchDao().getAll().stateIn(scope, STARTED, emptyList())

    val mortalities: StateFlow<List<MortalityEntity>> =
        database.mortalityDao().getAll().stateIn(scope, STARTED, emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> =
        database.expenseDao().getAll().stateIn(scope, STARTED, emptyList())

    val feedings: StateFlow<List<FeedingEntity>> =
        database.feedingDao().getAll().stateIn(scope, STARTED, emptyList())

    val weighings: StateFlow<List<WeighingEntity>> =
        database.weighingDao().getAll().stateIn(scope, STARTED, emptyList())

    val sales: StateFlow<List<SaleEntity>> =
        database.saleDao().getAll().stateIn(scope, STARTED, emptyList())

    val supplies: StateFlow<List<SupplyEntity>> =
        database.supplyDao().getAll().stateIn(scope, STARTED, emptyList())

    companion object {
        private val STARTED = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
    }
}
