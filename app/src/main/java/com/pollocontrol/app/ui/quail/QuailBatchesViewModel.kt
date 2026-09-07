package com.pollocontrol.app.ui.quail

import androidx.lifecycle.ViewModel
import com.pollocontrol.app.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import com.pollocontrol.app.data.local.entity.BatchEntity
import javax.inject.Inject

@HiltViewModel
class QuailBatchesViewModel @Inject constructor(
    private val batchRepository: BatchRepository
) : ViewModel() {
    val allBatches: Flow<List<BatchEntity>> = batchRepository.getAll()
}
