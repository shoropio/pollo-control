package com.pollocontrol.app.ui.inventory

import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.SupplyMovementEntity
import com.pollocontrol.app.domain.repository.SupplyMovementRepository
import com.pollocontrol.app.domain.repository.SupplyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var supplyRepository: SupplyRepository
    private lateinit var supplyMovementRepository: SupplyMovementRepository
    private lateinit var viewModel: InventoryViewModel

    private val suppliesFlow = MutableStateFlow<List<SupplyEntity>>(emptyList())
    private val lowStockFlow = MutableStateFlow<List<SupplyEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        supplyRepository = mock()
        supplyMovementRepository = mock()
        whenever(supplyRepository.getAll()).thenReturn(suppliesFlow)
        whenever(supplyRepository.getLowStock()).thenReturn(lowStockFlow)
        viewModel = InventoryViewModel(supplyRepository, supplyMovementRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `supplies flow emits repository data`() = runTest {
        val supply = SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO", stockActual = 100.0)
        suppliesFlow.value = listOf(supply)
        advanceUntilIdle()

        assertEquals(1, viewModel.supplies.value.size)
        assertEquals("Alimento", viewModel.supplies.value[0].nombre)
    }

    @Test
    fun `filteredSupplies shows all when filter is TODOS`() = runTest {
        suppliesFlow.value = listOf(
            SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO"),
            SupplyEntity(id = 2, nombre = "Vacuna", tipo = "SALUD")
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredSupplies.value.size)
    }

    @Test
    fun `setFilter ALIMENTO shows only feed supplies`() = runTest {
        suppliesFlow.value = listOf(
            SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO"),
            SupplyEntity(id = 2, nombre = "Vacuna", tipo = "SALUD")
        )
        viewModel.setFilter("ALIMENTO")
        advanceUntilIdle()

        val filtered = viewModel.filteredSupplies.value
        assertEquals(1, filtered.size)
        assertEquals("ALIMENTO", filtered[0].tipo)
    }

    @Test
    fun `setFilter SALUD shows only health supplies`() = runTest {
        suppliesFlow.value = listOf(
            SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO"),
            SupplyEntity(id = 2, nombre = "Vacuna", tipo = "SALUD")
        )
        viewModel.setFilter("SALUD")
        advanceUntilIdle()

        val filtered = viewModel.filteredSupplies.value
        assertEquals(1, filtered.size)
        assertEquals("SALUD", filtered[0].tipo)
    }

    @Test
    fun `lowStockSupplies emits from repository`() = runTest {
        val lowStock = SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO", stockActual = 5.0, stockMinimo = 10.0)
        lowStockFlow.value = listOf(lowStock)
        advanceUntilIdle()

        assertEquals(1, viewModel.lowStockSupplies.value.size)
    }

    @Test
    fun `save new supply calls insert`() = runTest {
        val supply = SupplyEntity(id = 0, nombre = "Nuevo", tipo = "ALIMENTO")
        whenever(supplyRepository.insert(supply)).thenReturn(1L)

        viewModel.save(supply) {}
        advanceUntilIdle()

        verify(supplyRepository).insert(supply)
    }

    @Test
    fun `save existing supply calls update`() = runTest {
        val supply = SupplyEntity(id = 5, nombre = "Existente", tipo = "SALUD")

        viewModel.save(supply) {}
        advanceUntilIdle()

        verify(supplyRepository).update(supply)
    }

    @Test
    fun `save invokes onSuccess callback`() = runTest {
        val supply = SupplyEntity(id = 0, nombre = "Nuevo", tipo = "ALIMENTO")
        whenever(supplyRepository.insert(supply)).thenReturn(1L)
        var callbackInvoked = false

        viewModel.save(supply) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `addStock calls repository addStock and inserts movement`() = runTest {
        viewModel.addStock(supplyId = 1L, amount = 50.0, cost = 100.0, notes = "Compra")
        advanceUntilIdle()

        verify(supplyRepository).addStock(1L, 50.0)
        verify(supplyMovementRepository).insert(any<SupplyMovementEntity>())
    }

    @Test
    fun `removeStock calls repository removeStock and inserts movement`() = runTest {
        viewModel.removeStock(supplyId = 1L, amount = 20.0, notes = "Uso en galpón")
        advanceUntilIdle()

        verify(supplyRepository).removeStock(1L, 20.0)
        verify(supplyMovementRepository).insert(any<SupplyMovementEntity>())
    }

    @Test
    fun `addStock creates ENTRADA movement`() = runTest {
        var capturedMovement: SupplyMovementEntity? = null
        whenever(supplyMovementRepository.insert(any())).thenAnswer { invocation ->
            capturedMovement = invocation.getArgument(0)
            1L
        }

        viewModel.addStock(supplyId = 1L, amount = 50.0, cost = 100.0, notes = "Compra")
        advanceUntilIdle()

        assertEquals("ENTRADA", capturedMovement?.tipo)
        assertEquals(50.0, capturedMovement?.cantidad!!, 0.01)
        assertEquals(1L, capturedMovement?.insumoId)
    }

    @Test
    fun `removeStock creates SALIDA movement`() = runTest {
        var capturedMovement: SupplyMovementEntity? = null
        whenever(supplyMovementRepository.insert(any())).thenAnswer { invocation ->
            capturedMovement = invocation.getArgument(0)
            1L
        }

        viewModel.removeStock(supplyId = 2L, amount = 10.0, notes = "Uso")
        advanceUntilIdle()

        assertEquals("SALIDA", capturedMovement?.tipo)
        assertEquals(10.0, capturedMovement?.cantidad!!, 0.01)
        assertEquals(2L, capturedMovement?.insumoId)
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        val supply = SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO")

        viewModel.delete(supply) {}
        advanceUntilIdle()

        verify(supplyRepository).delete(supply)
    }

    @Test
    fun `delete invokes onSuccess callback`() = runTest {
        val supply = SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO")
        var callbackInvoked = false

        viewModel.delete(supply) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `getById returns supply from repository`() = runTest {
        val supply = SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO")
        whenever(supplyRepository.getById(1L)).thenReturn(supply)
        var result: SupplyEntity? = null

        viewModel.getById(1L) { result = it }
        advanceUntilIdle()

        assertEquals(supply, result)
    }

    @Test
    fun `getById returns null for nonexistent id`() = runTest {
        whenever(supplyRepository.getById(999L)).thenReturn(null)
        var result: SupplyEntity? = SupplyEntity(id = -1, nombre = "placeholder", tipo = "X")

        viewModel.getById(999L) { result = it }
        advanceUntilIdle()

        assertEquals(null, result)
    }

    @Test
    fun `filter returns empty when no supplies match`() = runTest {
        suppliesFlow.value = listOf(
            SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO")
        )
        viewModel.setFilter("SALUD")
        advanceUntilIdle()

        assertTrue(viewModel.filteredSupplies.value.isEmpty())
    }
}
