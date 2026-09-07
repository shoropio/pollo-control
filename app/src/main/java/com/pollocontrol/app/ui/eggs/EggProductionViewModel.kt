package com.pollocontrol.app.ui.eggs

import androidx.lifecycle.ViewModel
import com.pollocontrol.app.data.local.entity.EggProductionEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.EggProductionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import com.pollocontrol.app.data.local.entity.BatchEntity
import javax.inject.Inject

@HiltViewModel
class EggProductionViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val eggProductionRepository: EggProductionRepository
) : ViewModel() {
    val allBatches: Flow<List<BatchEntity>> = batchRepository.getAll()
    val allEggs: Flow<List<EggProductionEntity>> = eggProductionRepository.getAll()

    suspend fun insertEgg(item: EggProductionEntity) = eggProductionRepository.insert(item)
    suspend fun deleteEgg(item: EggProductionEntity) = eggProductionRepository.delete(item)
}
