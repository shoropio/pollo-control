package com.pollocontrol.app.ui.dashboard

import com.pollocontrol.app.data.cache.AppDataCache
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.data.local.entity.WeighingEntity
import com.pollocontrol.app.domain.model.DashboardStats
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dataCache: AppDataCache
    private lateinit var viewModel: DashboardViewModel

    private val batchesFlow = MutableStateFlow<List<BatchEntity>>(emptyList())
    private val mortalitiesFlow = MutableStateFlow<List<MortalityEntity>>(emptyList())
    private val expensesFlow = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private val feedingsFlow = MutableStateFlow<List<FeedingEntity>>(emptyList())
    private val weighingsFlow = MutableStateFlow<List<WeighingEntity>>(emptyList())
    private val salesFlow = MutableStateFlow<List<SaleEntity>>(emptyList())
    private val suppliesFlow = MutableStateFlow<List<SupplyEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataCache = mock()
        whenever(dataCache.batches).thenReturn(batchesFlow)
        whenever(dataCache.mortalities).thenReturn(mortalitiesFlow)
        whenever(dataCache.expenses).thenReturn(expensesFlow)
        whenever(dataCache.feedings).thenReturn(feedingsFlow)
        whenever(dataCache.weighings).thenReturn(weighingsFlow)
        whenever(dataCache.sales).thenReturn(salesFlow)
        whenever(dataCache.supplies).thenReturn(suppliesFlow)
        viewModel = DashboardViewModel(dataCache)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default DashboardStats`() = runTest {
        advanceUntilIdle()
        val stats = viewModel.stats.value
        assertEquals(DashboardStats(), stats)
    }

    @Test
    fun `initial isLoading is true then becomes false after loading`() = runTest {
        assertTrue(viewModel.isLoading.value)
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `stats computed correctly for single active batch`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(1, stats.activeLotes)
        assertEquals(1000, stats.totalLiveChickens)
    }

    @Test
    fun `mortality reduces live chickens`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        mortalitiesFlow.value = listOf(
            MortalityEntity(id = 1, loteId = 1, fecha = System.currentTimeMillis(), cantidad = 50)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(50, stats.totalMortality)
        assertEquals(950, stats.totalLiveChickens)
    }

    @Test
    fun `mortality rate computed correctly`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        mortalitiesFlow.value = listOf(
            MortalityEntity(id = 1, loteId = 1, fecha = System.currentTimeMillis(), cantidad = 100)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(10.0, stats.mortalityRate, 0.01)
    }

    @Test
    fun `expenses from batches and general summed correctly`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, loteId = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis()),
            ExpenseEntity(id = 2, loteId = null, tipo = "GENERAL", descripcion = "Luz", monto = 200.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(700.0, stats.totalExpenses, 0.01)
    }

    @Test
    fun `cost per chicken computed correctly`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, loteId = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(0.5, stats.costPerChicken, 0.01)
    }

    @Test
    fun `sales total computed correctly`() = runTest {
        val now = System.currentTimeMillis()
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = now, tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0),
            SaleEntity(id = 2, fecha = now, tipo = "POLLO", modalidad = "KG", cantidad = 50.0, precioUnitario = 10.0, total = 500.0)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(1500.0, stats.totalSales, 0.01)
    }

    @Test
    fun `estimated profit is sales minus expenses`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, loteId = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 300.0, fecha = System.currentTimeMillis())
        )
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(700.0, stats.estimatedProfit, 0.01)
    }

    @Test
    fun `inactive batches are excluded`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO"),
            BatchEntity(id = 2, nombre = "Lote 2", cantidadInicial = 500, estado = "CERRADO")
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(1, stats.activeLotes)
    }

    @Test
    fun `low stock items counted correctly`() = runTest {
        suppliesFlow.value = listOf(
            SupplyEntity(id = 1, nombre = "Alimento", tipo = "ALIMENTO", stockActual = 5.0, stockMinimo = 10.0),
            SupplyEntity(id = 2, nombre = "Vacuna", tipo = "SALUD", stockActual = 20.0, stockMinimo = 5.0)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(1, stats.lowStockItems)
    }

    @Test
    fun `pending payments counted correctly`() = runTest {
        salesFlow.value = listOf(
            SaleEntity(id = 1, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 10.0, precioUnitario = 10.0, total = 100.0, estadoPago = "PENDIENTE"),
            SaleEntity(id = 2, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 10.0, precioUnitario = 10.0, total = 100.0, estadoPago = "PAGADO"),
            SaleEntity(id = 3, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 10.0, precioUnitario = 10.0, total = 100.0, estadoPago = "CREDITO")
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(2, stats.pendingPayments)
    }

    @Test
    fun `feed conversion computed correctly`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 1000, estado = "ACTIVO")
        )
        feedingsFlow.value = listOf(
            FeedingEntity(id = 1, loteId = 1, fecha = System.currentTimeMillis(), tipo = "INICIAL", cantidadKg = 500.0)
        )
        weighingsFlow.value = listOf(
            WeighingEntity(id = 1, loteId = 1, fecha = System.currentTimeMillis(), edadLote = 30, cantidadAves = 980, pesoPromedio = 2.0)
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        // feedConversion = 500 / (980 * 2.0) = 0.2551
        assertEquals(500.0 / (980 * 2.0), stats.feedConversion, 0.01)
        assertEquals(2.0, stats.averageWeight, 0.01)
    }

    @Test
    fun `loadDashboard with forceRefresh updates stats`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)

        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 500, estado = "ACTIVO")
        )
        viewModel.loadDashboard(forceRefresh = true)
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(1, stats.activeLotes)
        assertEquals(500, stats.totalLiveChickens)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `zero alive chickens yields zero costPerChicken`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote 1", cantidadInicial = 100, estado = "ACTIVO")
        )
        mortalitiesFlow.value = listOf(
            MortalityEntity(id = 1, loteId = 1, fecha = System.currentTimeMillis(), cantidad = 100)
        )
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, loteId = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()

        val stats = viewModel.stats.value
        assertEquals(0.0, stats.costPerChicken, 0.01)
    }

    @Test
    fun `no batches yields zero stats`() = runTest {
        advanceUntilIdle()
        val stats = viewModel.stats.value
        assertEquals(0, stats.activeLotes)
        assertEquals(0, stats.totalLiveChickens)
        assertEquals(0.0, stats.totalExpenses, 0.01)
        assertEquals(0.0, stats.totalSales, 0.01)
        assertEquals(0.0, stats.mortalityRate, 0.01)
    }
}
