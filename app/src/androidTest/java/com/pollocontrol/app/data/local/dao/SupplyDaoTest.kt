package com.pollocontrol.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.SupplyEntity
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
class SupplyDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PolloControlDatabase
    private lateinit var dao: SupplyDao

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = PolloControlDatabase.getDatabase(context)
        dao = database.supplyDao()
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createSupply(
        nombre: String = "Insumo Test",
        tipo: String = "ALIMENTO",
        stockActual: Double = 50.0,
        stockMinimo: Double = 10.0,
        unidad: String = "kg",
        observaciones: String = ""
    ) = SupplyEntity(
        nombre = nombre,
        tipo = tipo,
        stockActual = stockActual,
        stockMinimo = stockMinimo,
        unidad = unidad,
        observaciones = observaciones
    )

    @Test
    fun insertAndGetById() = runTest {
        val supply = createSupply()
        val id = dao.insert(supply)
        assertTrue(id > 0)

        val retrieved = dao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Insumo Test", retrieved!!.nombre)
        assertEquals(50.0, retrieved.stockActual, 0.01)
    }

    @Test
    fun insertAndGetAll() = runTest {
        dao.insert(createSupply(nombre = "Insumo 1"))
        dao.insert(createSupply(nombre = "Insumo 2"))

        val all = dao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun updateSupply() = runTest {
        val id = dao.insert(createSupply())
        val retrieved = dao.getById(id)!!

        val updated = retrieved.copy(nombre = "Insumo Actualizado", stockActual = 100.0)
        dao.update(updated)

        val result = dao.getById(id)!!
        assertEquals("Insumo Actualizado", result.nombre)
        assertEquals(100.0, result.stockActual, 0.01)
    }

    @Test
    fun deleteSupply() = runTest {
        val id = dao.insert(createSupply())
        val supply = dao.getById(id)!!
        dao.delete(supply)

        val result = dao.getById(id)
        assertNull(result)
    }

    @Test
    fun deleteById() = runTest {
        val id = dao.insert(createSupply())
        dao.deleteById(id)

        assertNull(dao.getById(id))
    }

    @Test
    fun getLowStock() = runTest {
        dao.insert(createSupply(nombre = "Normal", stockActual = 50.0, stockMinimo = 10.0))
        dao.insert(createSupply(nombre = "Low", stockActual = 5.0, stockMinimo = 10.0))
        dao.insert(createSupply(nombre = "Empty", stockActual = 0.0, stockMinimo = 10.0))

        val lowStock = dao.getLowStock().first()
        assertEquals(2, lowStock.size)
        assertTrue(lowStock.all { it.stockActual <= it.stockMinimo })
    }

    @Test
    fun getByType() = runTest {
        dao.insert(createSupply(nombre = "Alimento 1", tipo = "ALIMENTO"))
        dao.insert(createSupply(nombre = "Alimento 2", tipo = "ALIMENTO"))
        dao.insert(createSupply(nombre = "Vacuna 1", tipo = "VETERINARIO"))

        val alimentos = dao.getByType("ALIMENTO").first()
        assertEquals(2, alimentos.size)
        assertTrue(alimentos.all { it.tipo == "ALIMENTO" })
    }

    @Test
    fun addStock() = runTest {
        val id = dao.insert(createSupply(stockActual = 50.0))
        dao.addStock(id, 25.0)

        val result = dao.getById(id)!!
        assertEquals(75.0, result.stockActual, 0.01)
    }

    @Test
    fun removeStock() = runTest {
        val id = dao.insert(createSupply(stockActual = 50.0))
        dao.removeStock(id, 20.0)

        val result = dao.getById(id)!!
        assertEquals(30.0, result.stockActual, 0.01)
    }

    @Test
    fun replaceOnConflict() = runTest {
        val supply = createSupply()
        val id = dao.insert(supply)

        val replaced = supply.copy(id = id, nombre = "Reemplazado")
        val newId = dao.insert(replaced)
        assertEquals(id, newId)

        val result = dao.getById(id)!!
        assertEquals("Reemplazado", result.nombre)
    }
}
