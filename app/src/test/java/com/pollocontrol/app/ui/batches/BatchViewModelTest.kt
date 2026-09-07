package com.pollocontrol.app.ui.batches

import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var batchRepository: BatchRepository
    private lateinit var viewModel: BatchViewModel

    private val batchesFlow = MutableStateFlow<List<BatchEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        batchRepository = mock()
        whenever(batchRepository.getAll()).thenReturn(batchesFlow)
        viewModel = BatchViewModel(batchRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `batches flow emits repository data`() = runTest {
        val batch = BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        batchesFlow.value = listOf(batch)
        advanceUntilIdle()

        assertEquals(1, viewModel.batches.value.size)
        assertEquals("Lote 1", viewModel.batches.value[0].nombre)
    }

    @Test
    fun `filteredBatches shows all when filter is TODOS`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", estado = "ACTIVO"),
            BatchEntity(id = 2, nombre = "Lote 2", estado = "CERRADO")
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredBatches.value.size)
    }

    @Test
    fun `setFilter ACTIVO shows only active batches`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", estado = "ACTIVO"),
            BatchEntity(id = 2, nombre = "Lote 2", estado = "CERRADO")
        )
        viewModel.setFilter("ACTIVO")
        advanceUntilIdle()

        val filtered = viewModel.filteredBatches.value
        assertEquals(1, filtered.size)
        assertEquals("ACTIVO", filtered[0].estado)
    }

    @Test
    fun `setFilter CERRADO shows only closed batches`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", estado = "ACTIVO"),
            BatchEntity(id = 2, nombre = "Lote 2", estado = "CERRADO")
        )
        viewModel.setFilter("CERRADO")
        advanceUntilIdle()

        val filtered = viewModel.filteredBatches.value
        assertEquals(1, filtered.size)
        assertEquals("CERRADO", filtered[0].estado)
    }

    @Test
    fun `save new batch calls insert`() = runTest {
        val batch = BatchEntity(id = 0, nombre = "Nuevo Lote", cantidadInicial = 500)
        whenever(batchRepository.insert(batch)).thenReturn(1L)

        viewModel.save(batch) {}
        advanceUntilIdle()

        verify(batchRepository).insert(batch)
    }

    @Test
    fun `save existing batch calls update`() = runTest {
        val batch = BatchEntity(id = 5, nombre = "Lote Existente", cantidadInicial = 500)

        viewModel.save(batch) {}
        advanceUntilIdle()

        verify(batchRepository).update(batch)
    }

    @Test
    fun `save invokes onSuccess callback`() = runTest {
        val batch = BatchEntity(id = 0, nombre = "Nuevo Lote", cantidadInicial = 500)
        whenever(batchRepository.insert(batch)).thenReturn(1L)
        var callbackInvoked = false

        viewModel.save(batch) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        val batch = BatchEntity(id = 1, nombre = "Lote 1")

        viewModel.delete(batch) {}
        advanceUntilIdle()

        verify(batchRepository).delete(batch)
    }

    @Test
    fun `delete invokes onSuccess callback`() = runTest {
        val batch = BatchEntity(id = 1, nombre = "Lote 1")
        var callbackInvoked = false

        viewModel.delete(batch) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `getById returns batch from repository`() = runTest {
        val batch = BatchEntity(id = 1, nombre = "Lote 1")
        whenever(batchRepository.getById(1L)).thenReturn(batch)
        var result: BatchEntity? = null

        viewModel.getById(1L) { result = it }
        advanceUntilIdle()

        assertEquals(batch, result)
    }

    @Test
    fun `getById returns null for nonexistent id`() = runTest {
        whenever(batchRepository.getById(999L)).thenReturn(null)
        var result: BatchEntity? = BatchEntity(id = -1, nombre = "placeholder")

        viewModel.getById(999L) { result = it }
        advanceUntilIdle()

        assertEquals(null, result)
    }

    @Test
    fun `empty list from repository yields empty batches`() = runTest {
        batchesFlow.value = emptyList()
        advanceUntilIdle()

        assertTrue(viewModel.batches.value.isEmpty())
        assertTrue(viewModel.filteredBatches.value.isEmpty())
    }

    @Test
    fun `filter returns empty when no batches match`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", estado = "ACTIVO")
        )
        viewModel.setFilter("CERRADO")
        advanceUntilIdle()

        assertTrue(viewModel.filteredBatches.value.isEmpty())
    }
}
