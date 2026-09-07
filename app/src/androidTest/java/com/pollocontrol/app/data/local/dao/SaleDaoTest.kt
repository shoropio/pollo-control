package com.pollocontrol.app.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
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
class SaleDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PolloControlDatabase
    private lateinit var saleDao: SaleDao
    private lateinit var batchDao: BatchDao
    private lateinit var clientDao: ClientDao

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = PolloControlDatabase.getDatabase(context)
        saleDao = database.saleDao()
        batchDao = database.batchDao()
        clientDao = database.clientDao()
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

    private suspend fun createClient(nombre: String = "Cliente Test"): Long {
        return clientDao.insert(
            ClientEntity(
                nombre = nombre,
                telefono = "123456789",
                direccion = "Direccion 1",
                saldoPendiente = 0.0,
                observaciones = ""
            )
        )
    }

    private fun createSale(
        loteId: Long? = null,
        clienteId: Long? = null,
        fecha: Long = 1000L,
        tipo: String = "POLLO",
        modalidad: String = "KILO",
        cantidad: Double = 10.0,
        precioUnitario: Double = 5.0,
        total: Double = 50.0,
        metodoPago: String = "EFECTIVO",
        estadoPago: String = "PAGADO",
        observaciones: String = ""
    ) = SaleEntity(
        loteId = loteId,
        clienteId = clienteId,
        fecha = fecha,
        tipo = tipo,
        modalidad = modalidad,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        total = total,
        metodoPago = metodoPago,
        estadoPago = estadoPago,
        observaciones = observaciones
    )

    @Test
    fun insertAndGetAll() = runTest {
        val batchId = createBatch()
        val clientId = createClient()

        saleDao.insert(createSale(loteId = batchId, clienteId = clientId, total = 50.0))
        saleDao.insert(createSale(loteId = batchId, clienteId = clientId, total = 75.0, fecha = 2000L))

        val all = saleDao.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun updateSale() = runTest {
        val id = saleDao.insert(createSale(total = 50.0))
        val retrieved = saleDao.getAll().first().first { it.id == id }

        val updated = retrieved.copy(total = 100.0, observaciones = "Actualizado")
        saleDao.update(updated)

        val result = saleDao.getAll().first().first { it.id == id }
        assertEquals(100.0, result.total, 0.01)
        assertEquals("Actualizado", result.observaciones)
    }

    @Test
    fun deleteSale() = runTest {
        val id = saleDao.insert(createSale())
        val sale = saleDao.getAll().first().first { it.id == id }
        saleDao.delete(sale)

        val result = saleDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById() = runTest {
        val id = saleDao.insert(createSale())
        saleDao.deleteById(id)

        val result = saleDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getByBatch() = runTest {
        val batchId = createBatch()
        val otherBatchId = createBatch(nombre = "Lote 2")

        saleDao.insert(createSale(loteId = batchId, total = 50.0))
        saleDao.insert(createSale(loteId = batchId, total = 75.0))
        saleDao.insert(createSale(loteId = otherBatchId, total = 100.0))

        val result = saleDao.getByBatch(batchId).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.loteId == batchId })
    }

    @Test
    fun getByClient() = runTest {
        val clientId = createClient()
        val otherClientId = createClient(nombre = "Cliente 2")

        saleDao.insert(createSale(clienteId = clientId, total = 50.0))
        saleDao.insert(createSale(clienteId = clientId, total = 75.0))
        saleDao.insert(createSale(clienteId = otherClientId, total = 100.0))

        val result = saleDao.getByClient(clientId).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.clienteId == clientId })
    }

    @Test
    fun getTotal() = runTest {
        saleDao.insert(createSale(total = 50.0))
        saleDao.insert(createSale(total = 75.0))

        val total = saleDao.getTotal()
        assertEquals(125.0, total, 0.01)
    }

    @Test
    fun getTotalBetweenDates() = runTest {
        saleDao.insert(createSale(fecha = 1000L, total = 50.0))
        saleDao.insert(createSale(fecha = 2000L, total = 75.0))
        saleDao.insert(createSale(fecha = 3000L, total = 100.0))

        val total = saleDao.getTotalBetweenDates(1000L, 2000L)
        assertEquals(125.0, total, 0.01)
    }

    @Test
    fun getTotalSince() = runTest {
        saleDao.insert(createSale(fecha = 1000L, total = 50.0))
        saleDao.insert(createSale(fecha = 2000L, total = 75.0))
        saleDao.insert(createSale(fecha = 3000L, total = 100.0))

        val total = saleDao.getTotalSince(2000L)
        assertEquals(175.0, total, 0.01)
    }

    @Test
    fun getPending() = runTest {
        saleDao.insert(createSale(estadoPago = "PAGADO"))
        saleDao.insert(createSale(estadoPago = "PENDIENTE"))
        saleDao.insert(createSale(estadoPago = "CREDITO"))

        val pending = saleDao.getPending().first()
        assertEquals(2, pending.size)
        assertTrue(pending.all { it.estadoPago == "PENDIENTE" || it.estadoPago == "CREDITO" })
    }

    @Test
    fun replaceOnConflict() = runTest {
        val sale = createSale(total = 50.0)
        val id = saleDao.insert(sale)

        val replaced = sale.copy(id = id, total = 999.0)
        val newId = saleDao.insert(replaced)
        assertEquals(id, newId)

        val result = saleDao.getAll().first().first { it.id == id }
        assertEquals(999.0, result.total, 0.01)
    }
}
