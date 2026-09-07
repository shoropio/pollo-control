package com.pollocontrol.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.BatchEntity
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
class BatchDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PolloControlDatabase
    private lateinit var dao: BatchDao

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = PolloControlDatabase.getDatabase(context)
        dao = database.batchDao()
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createBatch(
        nombre: String = "Lote Test",
        fechaIngreso: Long = 1000L,
        cantidadInicial: Int = 100,
        especie: String = "POLLO",
        proposito: String = "CARNE",
        precioPorPollito: Double = 5.0,
        raza: String = "Cobb",
        galpon: String = "A1",
        proveedor: String = "Proveedor1",
        estado: String = "ACTIVO",
        observaciones: String = ""
    ) = BatchEntity(
        nombre = nombre,
        fechaIngreso = fechaIngreso,
        cantidadInicial = cantidadInicial,
        especie = especie,
        proposito = proposito,
        precioPorPollito = precioPorPollito,
        raza = raza,
        galpon = galpon,
        proveedor = proveedor,
        estado = estado,
        observaciones = observaciones
    )

    @Test
    fun insertAndGetById() = runTest {
        val batch = createBatch()
        val id = dao.insert(batch)
        assertTrue(id > 0)

        val retrieved = dao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Lote Test", retrieved!!.nombre)
        assertEquals(100, retrieved.cantidadInicial)
        assertEquals("POLLO", retrieved.especie)
        assertEquals("ACTIVO", retrieved.estado)
    }

    @Test
    fun insertAndGetAll() = runTest {
        dao.insert(createBatch(nombre = "Lote 1", fechaIngreso = 2000L))
        dao.insert(createBatch(nombre = "Lote 2", fechaIngreso = 1000L))

        val all = dao.getAll().first()
        assertEquals(2, all.size)
        assertEquals("Lote 1", all[0].nombre)
        assertEquals("Lote 2", all[1].nombre)
    }

    @Test
    fun updateBatch() = runTest {
        val batch = createBatch()
        val id = dao.insert(batch)

        val retrieved = dao.getById(id)!!
        val updated = retrieved.copy(nombre = "Lote Actualizado", estado = "CERRADO")
        dao.update(updated)

        val result = dao.getById(id)!!
        assertEquals("Lote Actualizado", result.nombre)
        assertEquals("CERRADO", result.estado)
    }

    @Test
    fun deleteBatch() = runTest {
        val batch = createBatch()
        val id = dao.insert(batch)

        val retrieved = dao.getById(id)!!
        dao.delete(retrieved)

        val result = dao.getById(id)
        assertNull(result)
    }

    @Test
    fun deleteById() = runTest {
        val id = dao.insert(createBatch())
        dao.deleteById(id)

        assertNull(dao.getById(id))
    }

    @Test
    fun getByEstado() = runTest {
        dao.insert(createBatch(nombre = "Activo 1", estado = "ACTIVO"))
        dao.insert(createBatch(nombre = "Activo 2", estado = "ACTIVO"))
        dao.insert(createBatch(nombre = "Cerrado 1", estado = "CERRADO"))

        val activos = dao.getByEstado("ACTIVO").first()
        assertEquals(2, activos.size)
        assertTrue(activos.all { it.estado == "ACTIVO" })

        val cerrados = dao.getByEstado("CERRADO").first()
        assertEquals(1, cerrados.size)
    }

    @Test
    fun getActiveCount() = runTest {
        dao.insert(createBatch(estado = "ACTIVO"))
        dao.insert(createBatch(estado = "ACTIVO"))
        dao.insert(createBatch(estado = "CERRADO"))

        val count = dao.getActiveCount().first()
        assertEquals(2, count)
    }

    @Test
    fun getByIdFlow() = runTest {
        val id = dao.insert(createBatch())
        val flow = dao.getByIdFlow(id).first()
        assertNotNull(flow)
        assertEquals("Lote Test", flow!!.nombre)

        assertNull(dao.getByIdFlow(999L).first())
    }

    @Test
    fun replaceOnConflict() = runTest {
        val batch = createBatch()
        val id = dao.insert(batch)

        val replaced = batch.copy(id = id, nombre = "Reemplazado")
        val newId = dao.insert(replaced)
        assertEquals(id, newId)

        val result = dao.getById(id)!!
        assertEquals("Reemplazado", result.nombre)
    }
}
