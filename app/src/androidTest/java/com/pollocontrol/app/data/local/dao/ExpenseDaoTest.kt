package com.pollocontrol.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PolloControlDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var batchDao: BatchDao

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = PolloControlDatabase.getDatabase(context)
        expenseDao = database.expenseDao()
        batchDao = database.batchDao()
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    private suspend fun createBatch(nombre: String = "Lote Test"): Long {
        return batchDao.insert(
            BatchEntity(
                nombre = nombre,
                fechaIngreso = 1000L,
                cantidadInicial = 100,
                especie = "POLLO",
                proposito = "CARNE",
                precioPorPollito = 5.0,
                raza = "Cobb",
                galpon = "A1",
                proveedor = "Proveedor1",
                estado = "ACTIVO",
                observaciones = ""
            )
        )
    }

    private fun createExpense(
        loteId: Long? = null,
        tipo: String = "ALIMENTO",
        descripcion: String = "Compra de alimento",
        monto: Double = 100.0,
        fecha: Long = 1000L,
        observaciones: String = ""
    ) = ExpenseEntity(
        loteId = loteId,
        tipo = tipo,
        descripcion = descripcion,
        monto = monto,
        fecha = fecha,
        observaciones = observaciones
    )

    @Test
    fun insertAndGetAll() = runTest {
        expenseDao.insert(createExpense(monto = 100.0))
        expenseDao.insert(createExpense(monto = 200.0, fecha = 2000L))

        val all = expenseDao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun updateExpense() = runTest {
        val id = expenseDao.insert(createExpense(monto = 100.0))
        val retrieved = expenseDao.getAll().first().first { it.id == id }

        val updated = retrieved.copy(monto = 250.0, descripcion = "Actualizado")
        expenseDao.update(updated)

        val result = expenseDao.getAll().first().first { it.id == id }
        assertEquals(250.0, result.monto, 0.01)
        assertEquals("Actualizado", result.descripcion)
    }

    @Test
    fun deleteExpense() = runTest {
        val id = expenseDao.insert(createExpense())
        val expense = expenseDao.getAll().first().first { it.id == id }
        expenseDao.delete(expense)

        val result = expenseDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById() = runTest {
        val id = expenseDao.insert(createExpense())
        expenseDao.deleteById(id)

        val result = expenseDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getByBatch() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        expenseDao.insert(createExpense(loteId = batchId, monto = 100.0))
        expenseDao.insert(createExpense(loteId = batchId, monto = 200.0))
        expenseDao.insert(createExpense(loteId = otherBatchId, monto = 300.0))

        val result = expenseDao.getByBatch(batchId).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.loteId == batchId })
    }

    @Test
    fun getGeneral() = runTest {
        val batchId = createBatch()

        expenseDao.insert(createExpense(loteId = batchId, monto = 100.0))
        expenseDao.insert(createExpense(loteId = null, monto = 200.0))
        expenseDao.insert(createExpense(loteId = null, monto = 300.0))

        val result = expenseDao.getGeneral().first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.loteId == null })
    }

    @Test
    fun getTotal() = runTest {
        expenseDao.insert(createExpense(monto = 100.0))
        expenseDao.insert(createExpense(monto = 200.0))

        val total = expenseDao.getTotal()
        assertEquals(300.0, total, 0.01)
    }

    @Test
    fun getTotalByBatch() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        expenseDao.insert(createExpense(loteId = batchId, monto = 100.0))
        expenseDao.insert(createExpense(loteId = batchId, monto = 200.0))
        expenseDao.insert(createExpense(loteId = otherBatchId, monto = 300.0))

        val total = expenseDao.getTotalByBatch(batchId)
        assertEquals(300.0, total, 0.01)
    }

    @Test
    fun getTotalGeneral() = runTest {
        val batchId = createBatch()

        expenseDao.insert(createExpense(loteId = batchId, monto = 100.0))
        expenseDao.insert(createExpense(loteId = null, monto = 200.0))
        expenseDao.insert(createExpense(loteId = null, monto = 300.0))

        val total = expenseDao.getTotalGeneral()
        assertEquals(500.0, total, 0.01)
    }

    @Test
    fun getTotalBetweenDates() = runTest {
        expenseDao.insert(createExpense(fecha = 1000L, monto = 100.0))
        expenseDao.insert(createExpense(fecha = 2000L, monto = 200.0))
        expenseDao.insert(createExpense(fecha = 3000L, monto = 300.0))

        val total = expenseDao.getTotalBetweenDates(1000L, 2000L)
        assertEquals(300.0, total, 0.01)
    }

    @Test
    fun getBetweenDatesFlow() = runTest {
        expenseDao.insert(createExpense(fecha = 1000L, monto = 100.0))
        expenseDao.insert(createExpense(fecha = 2000L, monto = 200.0))
        expenseDao.insert(createExpense(fecha = 3000L, monto = 300.0))

        val result = expenseDao.getBetweenDatesFlow(1000L, 2000L).first()
        assertEquals(2, result.size)
    }

    @Test
    fun getTotalByTypeAndBatch() = runTest {
        val batchId = createBatch()

        expenseDao.insert(createExpense(loteId = batchId, tipo = "ALIMENTO", monto = 100.0))
        expenseDao.insert(createExpense(loteId = batchId, tipo = "ALIMENTO", monto = 200.0))
        expenseDao.insert(createExpense(loteId = batchId, tipo = "VETERINARIO", monto = 300.0))

        val totalAlimento = expenseDao.getTotalByTypeAndBatch("ALIMENTO", batchId)
        assertEquals(300.0, totalAlimento, 0.01)

        val totalVeterinario = expenseDao.getTotalByTypeAndBatch("VETERINARIO", batchId)
        assertEquals(300.0, totalVeterinario, 0.01)
    }

    @Test
    fun replaceOnConflict() = runTest {
        val expense = createExpense(monto = 100.0)
        val id = expenseDao.insert(expense)

        val replaced = expense.copy(id = id, monto = 999.0)
        val newId = expenseDao.insert(replaced)
        assertEquals(id, newId)

        val result = expenseDao.getAll().first().first { it.id == id }
        assertEquals(999.0, result.monto, 0.01)
    }
}
