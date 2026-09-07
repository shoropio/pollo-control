@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.settings.formatMoney
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun ClientFormScreen(
    clienteId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: ClientsViewModel = hiltViewModel()
    val isEditing = clienteId != 0L
    val clientSales by viewModel.clientSales.collectAsState()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var saldoPendiente by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    LaunchedEffect(clienteId) {
        if (isEditing) {
            viewModel.getById(clienteId) { cliente ->
                if (cliente != null) {
                    nombre = cliente.nombre; telefono = cliente.telefono; direccion = cliente.direccion
                    saldoPendiente = cliente.saldoPendiente.toString(); observaciones = cliente.observaciones
                }
                isLoading = false
            }
            viewModel.loadClientSales(clienteId)
        } else isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Cliente" else "Nuevo Cliente") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = false }, label = { Text("Nombre *") }, isError = nombreError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = saldoPendiente, onValueChange = { saldoPendiente = it }, label = { Text("Saldo Pendiente") }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                Spacer(Modifier.height(8.dp))
                Button(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click@{
                    if (nombre.isBlank()) { nombreError = true; return@click }
                    viewModel.save(ClientEntity(
                        id = if (isEditing) clienteId else 0L,
                        nombre = nombre, telefono = telefono, direccion = direccion,
                        saldoPendiente = saldoPendiente.toDoubleOrNull() ?: 0.0, observaciones = observaciones
                    )) { onNavigateBack() }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }

                if (isEditing) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    OutlinedButton(
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar"); Spacer(Modifier.width(8.dp)); Text("Eliminar Cliente")
                    }
                    if (showDeleteConfirm) {
                        ConfirmDialog(
                            title = "Eliminar Cliente",
                            message = "Esta accion no se puede deshacer.",
                            onConfirm = {
                                viewModel.getById(clienteId) { c ->
                                    if (c != null) viewModel.delete(c) { onNavigateBack() }
                                }
                            },
                            onDismiss = { showDeleteConfirm = false }
                        )
                    }

                    if (clientSales.isNotEmpty()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Historial de Compras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        clientSales.forEach { venta ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(venta.fecha))} - ${formatMoney(venta.total, currency)}", style = MaterialTheme.typography.bodyMedium)
                                    Text("${String.format("%.2f", venta.cantidad)} ${if (venta.modalidad == "KILO") "kg" else "unid"} - ${venta.estadoPago}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
