package com.pollocontrol.app.ui.expenses

import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.domain.repository.BatchRepository
import com.pollocontrol.app.domain.repository.ExpenseRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var batchRepository: BatchRepository
    private lateinit var viewModel: ExpensesViewModel

    private val expensesFlow = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private val batchesFlow = MutableStateFlow<List<BatchEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        expenseRepository = mock()
        batchRepository = mock()
        whenever(expenseRepository.getAll()).thenReturn(expensesFlow)
        whenever(batchRepository.getAll()).thenReturn(batchesFlow)
        viewModel = ExpensesViewModel(expenseRepository, batchRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expenses flow emits repository data`() = runTest {
        val expense = ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        expensesFlow.value = listOf(expense)
        advanceUntilIdle()

        assertEquals(1, viewModel.expenses.value.size)
        assertEquals(500.0, viewModel.expenses.value[0].monto, 0.01)
    }

    @Test
    fun `totalExpenses computed correctly`() = runTest {
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis()),
            ExpenseEntity(id = 2, tipo = "SALUD", descripcion = "Vacunas", monto = 200.0, fecha = System.currentTimeMillis()),
            ExpenseEntity(id = 3, tipo = "GENERAL", descripcion = "Luz", monto = 100.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()

        assertEquals(800.0, viewModel.totalExpenses.value, 0.01)
    }

    @Test
    fun `totalExpenses is zero with no expenses`() = runTest {
        expensesFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals(0.0, viewModel.totalExpenses.value, 0.01)
    }

    @Test
    fun `batchMap built from batches`() = runTest {
        batchesFlow.value = listOf(
            BatchEntity(id = 1, nombre = "Lote A"),
            BatchEntity(id = 2, nombre = "Lote B")
        )
        advanceUntilIdle()

        val map = viewModel.batchMap.value
        assertEquals(2, map.size)
        assertEquals("Lote A", map[1L])
        assertEquals("Lote B", map[2L])
    }

    @Test
    fun `batchMap is empty with no batches`() = runTest {
        batchesFlow.value = emptyList()
        advanceUntilIdle()

        assertTrue(viewModel.batchMap.value.isEmpty())
    }

    @Test
    fun `save new expense calls insert`() = runTest {
        val expense = ExpenseEntity(id = 0, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        whenever(expenseRepository.insert(expense)).thenReturn(1L)

        viewModel.save(expense) {}
        advanceUntilIdle()

        verify(expenseRepository).insert(expense)
    }

    @Test
    fun `save existing expense calls update`() = runTest {
        val expense = ExpenseEntity(id = 5, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())

        viewModel.save(expense) {}
        advanceUntilIdle()

        verify(expenseRepository).update(expense)
    }

    @Test
    fun `save invokes onSuccess callback`() = runTest {
        val expense = ExpenseEntity(id = 0, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        whenever(expenseRepository.insert(expense)).thenReturn(1L)
        var callbackInvoked = false

        viewModel.save(expense) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        val expense = ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())

        viewModel.delete(expense) {}
        advanceUntilIdle()

        verify(expenseRepository).delete(expense)
    }

    @Test
    fun `delete invokes onSuccess callback`() = runTest {
        val expense = ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        var callbackInvoked = false

        viewModel.delete(expense) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `getById returns matching expense`() = runTest {
        val now = System.currentTimeMillis()
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = now),
            ExpenseEntity(id = 2, tipo = "SALUD", descripcion = "Vacunas", monto = 200.0, fecha = now)
        )
        var result: ExpenseEntity? = null

        viewModel.getById(2L) { result = it }
        advanceUntilIdle()

        assertEquals(2L, result?.id)
        assertEquals(200.0, result?.monto!!, 0.01)
    }

    @Test
    fun `getById returns null for nonexistent id`() = runTest {
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 500.0, fecha = System.currentTimeMillis())
        )
        var result: ExpenseEntity? = ExpenseEntity(id = -1, tipo = "X", descripcion = "X", monto = 0.0, fecha = 0L)

        viewModel.getById(999L) { result = it }
        advanceUntilIdle()

        assertEquals(null, result)
    }

    @Test
    fun `totalExpenses updates when expenses change`() = runTest {
        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 300.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()
        assertEquals(300.0, viewModel.totalExpenses.value, 0.01)

        expensesFlow.value = listOf(
            ExpenseEntity(id = 1, tipo = "ALIMENTO", descripcion = "Sacos", monto = 300.0, fecha = System.currentTimeMillis()),
            ExpenseEntity(id = 2, tipo = "GENERAL", descripcion = "Luz", monto = 150.0, fecha = System.currentTimeMillis())
        )
        advanceUntilIdle()
        assertEquals(450.0, viewModel.totalExpenses.value, 0.01)
    }
}
