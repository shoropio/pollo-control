package com.pollocontrol.app.ui.clients

import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ClientsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var clientRepository: ClientRepository
    private lateinit var saleRepository: SaleRepository
    private lateinit var viewModel: ClientsViewModel

    private val clientsFlow = MutableStateFlow<List<ClientEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clientRepository = mock()
        saleRepository = mock()
        whenever(clientRepository.getAll()).thenReturn(clientsFlow)
        viewModel = ClientsViewModel(clientRepository, saleRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clients flow emits repository data`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan Perez", telefono = "555-0001")
        clientsFlow.value = listOf(client)
        advanceUntilIdle()

        assertEquals(1, viewModel.clients.value.size)
        assertEquals("Juan Perez", viewModel.clients.value[0].nombre)
    }

    @Test
    fun `searchResults shows all when query is blank`() = runTest {
        clientsFlow.value = listOf(
            ClientEntity(id = 1, nombre = "Juan Perez"),
            ClientEntity(id = 2, nombre = "Maria Lopez")
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.searchResults.value.size)
    }

    @Test
    fun `search filters by name case insensitive`() = runTest {
        clientsFlow.value = listOf(
            ClientEntity(id = 1, nombre = "Juan Perez"),
            ClientEntity(id = 2, nombre = "Maria Lopez"),
            ClientEntity(id = 3, nombre = "juan Garcia")
        )
        viewModel.search("juan")
        advanceUntilIdle()

        val results = viewModel.searchResults.value
        assertEquals(2, results.size)
        assertTrue(results.all { it.nombre.contains("juan", ignoreCase = true) })
    }

    @Test
    fun `search filters by phone number`() = runTest {
        clientsFlow.value = listOf(
            ClientEntity(id = 1, nombre = "Juan Perez", telefono = "555-0001"),
            ClientEntity(id = 2, nombre = "Maria Lopez", telefono = "555-0002")
        )
        viewModel.search("0001")
        advanceUntilIdle()

        val results = viewModel.searchResults.value
        assertEquals(1, results.size)
        assertEquals("Juan Perez", results[0].nombre)
    }

    @Test
    fun `search returns empty when no match`() = runTest {
        clientsFlow.value = listOf(
            ClientEntity(id = 1, nombre = "Juan Perez"),
            ClientEntity(id = 2, nombre = "Maria Lopez")
        )
        viewModel.search("Pedro")
        advanceUntilIdle()

        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `search clears results when query is cleared`() = runTest {
        clientsFlow.value = listOf(
            ClientEntity(id = 1, nombre = "Juan Perez"),
            ClientEntity(id = 2, nombre = "Maria Lopez")
        )
        viewModel.search("Juan")
        advanceUntilIdle()
        assertEquals(1, viewModel.searchResults.value.size)

        viewModel.search("")
        advanceUntilIdle()
        assertEquals(2, viewModel.searchResults.value.size)
    }

    @Test
    fun `save new client calls insert`() = runTest {
        val client = ClientEntity(id = 0, nombre = "Nuevo Cliente")
        whenever(clientRepository.insert(client)).thenReturn(1L)

        viewModel.save(client) {}
        advanceUntilIdle()

        verify(clientRepository).insert(client)
    }

    @Test
    fun `save existing client calls update`() = runTest {
        val client = ClientEntity(id = 5, nombre = "Cliente Existente")

        viewModel.save(client) {}
        advanceUntilIdle()

        verify(clientRepository).update(client)
    }

    @Test
    fun `save invokes onSuccess callback`() = runTest {
        val client = ClientEntity(id = 0, nombre = "Nuevo Cliente")
        whenever(clientRepository.insert(client)).thenReturn(1L)
        var callbackInvoked = false

        viewModel.save(client) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `delete calls repository delete`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan Perez")

        viewModel.delete(client) {}
        advanceUntilIdle()

        verify(clientRepository).delete(client)
    }

    @Test
    fun `delete invokes onSuccess callback`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan Perez")
        var callbackInvoked = false

        viewModel.delete(client) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `getById returns client from repository`() = runTest {
        val client = ClientEntity(id = 1, nombre = "Juan Perez")
        whenever(clientRepository.getById(1L)).thenReturn(client)
        var result: ClientEntity? = null

        viewModel.getById(1L) { result = it }
        advanceUntilIdle()

        assertEquals(client, result)
    }

    @Test
    fun `getById returns null for nonexistent id`() = runTest {
        whenever(clientRepository.getById(999L)).thenReturn(null)
        var result: ClientEntity? = ClientEntity(id = -1, nombre = "placeholder")

        viewModel.getById(999L) { result = it }
        advanceUntilIdle()

        assertEquals(null, result)
    }

    @Test
    fun `loadClientSales collects from saleRepository`() = runTest {
        val sales = listOf(
            SaleEntity(id = 1, clienteId = 1L, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
        )
        val salesFlow = MutableStateFlow(sales)
        whenever(saleRepository.getByClient(1L)).thenReturn(salesFlow)

        viewModel.loadClientSales(1L)
        advanceUntilIdle()

        assertEquals(1, viewModel.clientSales.value.size)
        assertEquals(1000.0, viewModel.clientSales.value[0].total, 0.01)
    }

    @Test
    fun `loadClientSales updates when sales change`() = runTest {
        val salesFlow = MutableStateFlow(
            listOf(
                SaleEntity(id = 1, clienteId = 1L, fecha = System.currentTimeMillis(), tipo = "POLLO", modalidad = "KG", cantidad = 100.0, precioUnitario = 10.0, total = 1000.0)
            )
        )
        whenever(saleRepository.getByClient(1L)).thenReturn(salesFlow)

        viewModel.loadClientSales(1L)
        advanceUntilIdle()
        assertEquals(1, viewModel.clientSales.value.size)

        salesFlow.value = emptyList()
        advanceUntilIdle()
        assertTrue(viewModel.clientSales.value.isEmpty())
    }

    @Test
    fun `empty client list yields empty search results`() = runTest {
        clientsFlow.value = emptyList()
        advanceUntilIdle()

        assertTrue(viewModel.clients.value.isEmpty())
        assertTrue(viewModel.searchResults.value.isEmpty())
    }
}
