@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.weighing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.data.local.entity.WeighingEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import com.pollocontrol.app.ui.components.PolloEmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeighingScreen(
    batchId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: WeighingViewModel = hiltViewModel()

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val lote by viewModel.batch.collectAsState()
    val records by viewModel.records.collectAsState()
    val ultimoPesaje by viewModel.latestWeighing.collectAsState()
    var editingRecord by remember { mutableStateOf<WeighingEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<WeighingEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter {
            it.edadLote.toString().contains(searchQuery) ||
            it.cantidadAves.toString().contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesajes - ${lote?.nombre ?: ""}") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { editingRecord = WeighingEntity(loteId = batchId, fecha = System.currentTimeMillis(), edadLote = 0, cantidadAves = 0, pesoPromedio = 0.0) }) { Icon(Icons.Default.Add, "Registrar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ultimoPesaje?.let { ultimo ->
                Card(Modifier.fillMaxWidth().padding(16.dp)) { Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ultimo Pesaje", style = MaterialTheme.typography.bodySmall)
                    Text("${String.format("%.3f", ultimo.pesoPromedio)} kg", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${ultimo.cantidadAves} aves pesadas | Dia ${ultimo.edadLote}", style = MaterialTheme.typography.bodySmall)
                }}
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por dia o cantidad...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true
            )

            if (filteredRecords.isEmpty()) {
                PolloEmptyState(
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay registros de pesaje",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Registre un pesaje usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredRecords, key = { it.id }) { r ->
                        Card(Modifier.fillMaxWidth().clickable { editingRecord = r }) { Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dia ${r.edadLote}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showDeleteConfirm = r }) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                            }
                            Text("Peso promedio: ${String.format("%.3f", r.pesoPromedio)} kg", style = MaterialTheme.typography.bodyLarge)
                            Text("Aves pesadas: ${r.cantidadAves}", style = MaterialTheme.typography.bodyMedium)
                            Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(r.fecha)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }}
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        WeighingFormDialog(
            loteId = batchId,
            initial = if (record.id == 0L) null else record,
            onSave = { viewModel.save(it) { editingRecord = null } },
            onDismiss = { editingRecord = null }
        )
    }

    showDeleteConfirm?.let { r -> ConfirmDialog(title = "Eliminar pesaje", message = "Eliminar registro del dia ${r.edadLote}?", onConfirm = { viewModel.delete(r) { showDeleteConfirm = null } }, onDismiss = { showDeleteConfirm = null }) }
}

@Composable
private fun WeighingFormDialog(loteId: Long, initial: WeighingEntity? = null, onSave: (WeighingEntity) -> Unit, onDismiss: () -> Unit) {
    val isEditing = initial != null
    var edadLote by remember { mutableStateOf(initial?.edadLote?.toString() ?: "") }
    var cantidadAves by remember { mutableStateOf(initial?.cantidadAves?.toString() ?: "") }
    var pesoPromedio by remember { mutableStateOf(initial?.pesoPromedio?.toString() ?: "") }
    var fecha by remember { mutableStateOf(initial?.fecha ?: System.currentTimeMillis()) }
    var observaciones by remember { mutableStateOf(initial?.observaciones ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var edadError by remember { mutableStateOf(false) }
    var cantError by remember { mutableStateOf(false) }
    var pesoError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Pesaje" else "Nuevo Pesaje") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = edadLote, onValueChange = { edadLote = it; edadError = false }, label = { Text("Edad del Lote (dias) *") }, isError = edadError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = cantidadAves, onValueChange = { cantidadAves = it; cantError = false }, label = { Text("Aves Pesadas *") }, isError = cantError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = pesoPromedio, onValueChange = { pesoPromedio = it; pesoError = false }, label = { Text("Peso Promedio (kg) *") }, isError = pesoError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)), onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = {
                if (edadLote.isBlank() || edadLote.toIntOrNull() == null) { edadError = true; return@TextButton }
                if (cantidadAves.isBlank() || cantidadAves.toIntOrNull() == null) { cantError = true; return@TextButton }
                if (pesoPromedio.isBlank() || pesoPromedio.toDoubleOrNull() == null) { pesoError = true; return@TextButton }
                onSave(WeighingEntity(
                    id = initial?.id ?: 0L,
                    loteId = loteId, fecha = fecha,
                    edadLote = edadLote.toInt(),
                    cantidadAves = cantidadAves.toInt(),
                    pesoPromedio = pesoPromedio.toDouble(),
                    observaciones = observaciones
                ))
            }) { Text(if (isEditing) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onDismiss) { Text("Cancelar") } }
    )
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
