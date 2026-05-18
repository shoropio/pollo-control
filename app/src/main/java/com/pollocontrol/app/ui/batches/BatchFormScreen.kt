@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.batches

import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun BatchFormScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: BatchViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BatchViewModel(app) as T
        }
    )
    val isEditing = batchId != 0L

    var nombre by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf(System.currentTimeMillis()) }
    var cantidadInicial by remember { mutableStateOf("") }
    var precioPorPollito by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var galpon by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("ACTIVO") }
    var observaciones by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf(false) }
    var cantidadError by remember { mutableStateOf(false) }
    var precioError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }
    val currency by app.settingsManager.currency.collectAsState()

    LaunchedEffect(batchId) {
        if (isEditing) {
            viewModel.getById(batchId) { batch ->
                if (batch != null) {
                    nombre = batch.nombre
                    fechaIngreso = batch.fechaIngreso
                    cantidadInicial = batch.cantidadInicial.toString()
                    precioPorPollito = batch.precioPorPollito.toString()
                    raza = batch.raza
                    galpon = batch.galpon
                    proveedor = batch.proveedor
                    estado = batch.estado
                    observaciones = batch.observaciones
                }
                isLoading = false
            }
        } else isLoading = false
    }

    val estados = listOf("ACTIVO", "FINALIZADO", "VENDIDO", "SACRIFICADO")
    var expanded by remember { mutableStateOf(false) }
    val dateStr = remember(fechaIngreso) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaIngreso)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Lote" else "Nuevo Lote") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = false }, label = { Text("Nombre del Lote *") }, isError = nombreError, supportingText = if (nombreError) {{ Text("Campo requerido")}} else null, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(value = dateStr, onValueChange = {}, label = { Text("Fecha de Ingreso") }, modifier = Modifier.fillMaxWidth(), readOnly = true, trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Seleccionar fecha") } })

                OutlinedTextField(value = cantidadInicial, onValueChange = { cantidadInicial = it; cantidadError = false }, label = { Text("Cantidad Inicial *") }, isError = cantidadError, supportingText = if (cantidadError) {{ Text("Campo requerido")}} else null, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(value = precioPorPollito, onValueChange = { precioPorPollito = it; precioError = false }, label = { Text("Precio por Pollito *") }, isError = precioError, supportingText = if (precioError) {{ Text("Campo requerido")}} else null, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })

                OutlinedTextField(value = raza, onValueChange = { raza = it }, label = { Text("Raza / Línea Genética") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(value = galpon, onValueChange = { galpon = it }, label = { Text("Galpón / Ubicación") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(value = proveedor, onValueChange = { proveedor = it }, label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                // Estado dropdown
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = estado, onValueChange = {}, readOnly = true, label = { Text("Estado") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        estados.forEach { e ->
                            DropdownMenuItem(text = { Text(e.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { estado = e; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                Spacer(Modifier.height(8.dp))

                Button(onClick = click@{
                    var valid = true
                    if (nombre.isBlank()) { nombreError = true; valid = false }
                    if (cantidadInicial.isBlank() || cantidadInicial.toIntOrNull() == null || cantidadInicial.toInt() <= 0) { cantidadError = true; valid = false }
                    if (precioPorPollito.isBlank() || precioPorPollito.toDoubleOrNull() == null || precioPorPollito.toDouble() < 0) { precioError = true; valid = false }
                    if (!valid) return@click

                    viewModel.save(BatchEntity(
                        id = if (isEditing) batchId else 0L,
                        nombre = nombre,
                        fechaIngreso = fechaIngreso,
                        cantidadInicial = cantidadInicial.toInt(),
                        precioPorPollito = precioPorPollito.toDouble(),
                        raza = raza,
                        galpon = galpon,
                        proveedor = proveedor,
                        estado = estado,
                        observaciones = observaciones
                    )) { onNavigateBack() }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }

                if (isEditing) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar")
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar Lote")
                    }
                    if (showDeleteConfirm) {
                        ConfirmDialog(
                            title = "Eliminar Lote",
                            message = "Esta accion no se puede deshacer.",
                            onConfirm = {
                                viewModel.getById(batchId) { batch ->
                                    if (batch != null) viewModel.delete(batch) { onNavigateBack() }
                                }
                            },
                            onDismiss = { showDeleteConfirm = false }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        PolloDatePickerDialog(onDateSelected = { fechaIngreso = it }, onDismiss = { showDatePicker = false })
    }
}
