package com.pollocontrol.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.MortalityEntity
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
class MortalityDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PolloControlDatabase
    private lateinit var mortalityDao: MortalityDao
    private lateinit var batchDao: BatchDao

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = PolloControlDatabase.getDatabase(context)
        mortalityDao = database.mortalityDao()
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

    private fun createMortality(
        loteId: Long,
        fecha: Long = 1000L,
        cantidad: Int = 5,
        motivo: String = "Enfermedad",
        observaciones: String = ""
    ) = MortalityEntity(
        loteId = loteId,
        fecha = fecha,
        cantidad = cantidad,
        motivo = motivo,
        observaciones = observaciones
    )

    @Test
    fun insertAndGetAll() = runTest {
        val batchId = createBatch()
        mortalityDao.insert(createMortality(loteId = batchId))
        mortalityDao.insert(createMortality(loteId = batchId, fecha = 2000L))

        val all = mortalityDao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun updateMortality() = runTest {
        val batchId = createBatch()
        val id = mortalityDao.insert(createMortality(loteId = batchId))
        val retrieved = mortalityDao.getAll().first().first { it.id == id }

        val updated = retrieved.copy(cantidad = 10, motivo = "Otro motivo")
        mortalityDao.update(updated)

        val result = mortalityDao.getAll().first().first { it.id == id }
        assertEquals(10, result.cantidad)
        assertEquals("Otro motivo", result.motivo)
    }

    @Test
    fun deleteMortality() = runTest {
        val batchId = createBatch()
        val id = mortalityDao.insert(createMortality(loteId = batchId))
        val mortality = mortalityDao.getAll().first().first { it.id == id }
        mortalityDao.delete(mortality)

        val result = mortalityDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById() = runTest {
        val batchId = createBatch()
        val id = mortalityDao.insert(createMortality(loteId = batchId))
        mortalityDao.deleteById(id)

        val result = mortalityDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getByBatch() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        mortalityDao.insert(createMortality(loteId = batchId))
        mortalityDao.insert(createMortality(loteId = batchId))
        mortalityDao.insert(createMortality(loteId = otherBatchId))

        val result = mortalityDao.getByBatch(batchId).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.loteId == batchId })
    }

    @Test
    fun getTotalByBatch() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        mortalityDao.insert(createMortality(loteId = batchId, cantidad = 5))
        mortalityDao.insert(createMortality(loteId = batchId, cantidad = 10))
        mortalityDao.insert(createMortality(loteId = otherBatchId, cantidad = 20))

        val total = mortalityDao.getTotalByBatch(batchId)
        assertEquals(15, total)
    }

    @Test
    fun getTotalByBatchFlow() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        mortalityDao.insert(createMortality(loteId = batchId, cantidad = 5))
        mortalityDao.insert(createMortality(loteId = batchId, cantidad = 10))
        mortalityDao.insert(createMortality(loteId = otherBatchId, cantidad = 20))

        val totalFlow = mortalityDao.getTotalByBatchFlow(batchId).first()
        assertEquals(15, totalFlow)
    }

    @Test
    fun replaceOnConflict() = runTest {
        val batchId = createBatch()
        val mortality = createMortality(loteId = batchId)
        val id = mortalityDao.insert(mortality)

        val replaced = mortality.copy(id = id, cantidad = 99)
        val newId = mortalityDao.insert(replaced)
        assertEquals(id, newId)

        val result = mortalityDao.getAll().first().first { it.id == id }
        assertEquals(99, result.cantidad)
    }
}
