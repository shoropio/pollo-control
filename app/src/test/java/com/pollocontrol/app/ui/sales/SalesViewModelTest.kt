package com.pollocontrol.app.ui.sales

import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.ClientRepository
import com.pollocontrol.app.domain.repository.SaleRepository
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
class SalesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var saleRepository: SaleRepository
    private lateinit var clientRepository: ClientRepository
    private lateinit var batchRepository: BatchRepository
    private lateinit var viewModel: SalesViewModel

    private val salesFlow = MutableStateFlow<List<SaleEntity>>(emptyList())
    private val clientsFlow = MutableStateFlow<List<ClientEntity>>(emptyList())
    private val batchesFlow = MutableStateFlow<List<BatchEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        saleRepository = mock()
        clientRepository = mock()
        batchRepository = mock()
        whenever(saleRepository.getAll()).thenReturn(salesFlow)
        whenever(clientRepository.getAll()).thenReturn(clientsFlow)
        whenever(batchRepository.getAll()).thenReturn(batchesFlow)
        viewModel = SalesViewModel(saleRepository, clientRepository, batchRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sales flow emits repository data`() = runTest {
        val sale = SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        salesFlow.value = listOf(sale)
        advanceUntilIdle()

        assertEquals(1, viewModel.sales.value.size)
        assertEquals(1000.0, viewModel.sales.value[0].total, 0.01)
    }

    @Test
    fun `clients flow emits repository data`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan")
        clientsFlow.value = listOf(client)
        advanceUntilIdle()

        assertEquals(1, viewModel.clients.value.size)
        assertEquals("Juan", viewModel.clients.value[0].nombre)
    }

    @Test
    fun `batches flow emits repository data`() = runTest {
        val batch = BatchEntity(id = 1, nombre = "Lote 1")
        batchesFlow.value = listOf(batch)
        advanceUntilIdle()

        assertEquals(1, viewModel.batches.value.size)
    }

    @Test
    fun `totalSales computed correctly`() = runTest {
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0),
            SaleEntity(id = 2, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 50.0, precioUnitario = 20.0, total = 1000.0)
        )
        advanceUntilIdle()

        assertEquals(2000.0, viewModel.totalSales.value, 0.01)
    }

    @Test
    fun `totalSales is zero with no sales`() = runTest {
        salesFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals(0.0, viewModel.totalSales.value, 0.01)
    }

    @Test
    fun `totalSales updates when sales change`() = runTest {
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 10.0, precioUnitario = 10.0, total = 100.0)
        )
        advanceUntilIdle()
        assertEquals(100.0, viewModel.totalSales.value, 0.01)

        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 10.0, precioUnitario = 10.0, total = 100.0),
            SaleEntity(id = 2, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 20.0, precioUnitario = 15.0, total = 300.0)
        )
        advanceUntilIdle()
        assertEquals(400.0, viewModel.totalSales.value, 0.01)
    }

    @Test
    fun `save new sale calls insert`() = runTest {
        val sale = SaleEntity(id = 0, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        whenever(saleRepository.insert(sale)).thenReturn(1L)

        viewModel.save(sale) {}
        advanceUntilIdle()

        verify(saleRepository).insert(sale)
    }

    @Test
    fun `save existing sale calls update`() = runTest {
        val sale = SaleEntity(id = 5, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)

        viewModel.save(sale) {}
        advanceUntilIdle()

        verify(saleRepository).update(sale)
    }

    @Test
    fun `save credit sale updates client balance`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan", saldoPendiente = 0.0)
        val sale = SaleEntity(id = 0, clienteId = 1L, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0, estadoPago = "CREDITO")
        whenever(saleRepository.insert(sale)).thenReturn(1L)
        whenever(clientRepository.getById(1L)).thenReturn(client)

        viewModel.save(sale) {}
        advanceUntilIdle()

        verify(clientRepository).update(client.copy(saldoPendiente = 1000.0))
    }

    @Test
    fun `save pending sale updates client balance`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan", saldoPendiente = 500.0)
        val sale = SaleEntity(id = 0, clienteId = 1L, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 50.0, precioUnitario = 10.0, total = 500.0, estadoPago = "PENDIENTE")
        whenever(saleRepository.insert(sale)).thenReturn(1L)
        whenever(clientRepository.getById(1L)).thenReturn(client)

        viewModel.save(sale) {}
        advanceUntilIdle()

        verify(clientRepository).update(client.copy(saldoPendiente = 1000.0))
    }

    @Test
    fun `save paid sale does not update client balance`() = runTest {
        val sale = SaleEntity(id = 0, clienteId = 1L, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0, estadoPago = "PAGADO")
        whenever(saleRepository.insert(sale)).thenReturn(1L)

        viewModel.save(sale) {}
        advanceUntilIdle()

        verify(clientRepository, org.mockito.kotlin.never()).update(any())
    }

    @Test
    fun `save invokes onSuccess callback`() = runTest {
        val sale = SaleEntity(id = 0, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        whenever(saleRepository.insert(sale)).thenReturn(1L)
        var callbackInvoked = false

        viewModel.save(sale) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        val sale = SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)

        viewModel.delete(sale) {}
        advanceUntilIdle()

        verify(saleRepository).delete(sale)
    }

    @Test
    fun `delete invokes onSuccess callback`() = runTest {
        val sale = SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        var callbackInvoked = false

        viewModel.delete(sale) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `getById returns matching sale`() = runTest {
        val now = System.currentTimeMillis()
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = now, tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0),
            SaleEntity(id = 2, fecha = now, tipo = "HUEVO", modalidad = "DOCENA", cantidad = 10.0, precioUnitario = 5.0, total = 50.0)
        )
        var result: SaleEntity? = null

        viewModel.getById(2L) { result = it }
        advanceUntilIdle()

        assertEquals(2L, result?.id)
        assertEquals("HUEVO", result?.tipo)
    }

    @Test
    fun `getById returns null for nonexistent id`() = runTest {
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        )
        var result: SaleEntity? = SaleEntity(id = -1, fecha = 0, tipo = "X", modalidad = "X", cantidad = 0.0, precioUnitario = 0.0, total = 0.0)

        viewModel.getById(999L) { result = it }
        advanceUntilIdle()

        assertEquals(null, result)
    }
}
