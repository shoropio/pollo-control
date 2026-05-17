@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.slaughter

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.SlaughterEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import com.pollocontrol.app.ui.components.PolloEmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SlaughterScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: SlaughterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SlaughterViewModel(app) as T
        }
    )

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val lote by viewModel.batch.collectAsState()
    val records by viewModel.records.collectAsState()
    val totalSacrificados by viewModel.totalSlaughtered.collectAsState()
    var editingRecord by remember { mutableStateOf<SlaughterEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<SlaughterEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter {
            it.cantidad.toString().contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sacrificio - ${lote?.nombre ?: ""}") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingRecord = SlaughterEntity(loteId = batchId, fecha = System.currentTimeMillis(), cantidad = 0, pesoVivoPromedio = 0.0, pesoCanalPromedio = 0.0) }) { Icon(Icons.Default.Add, "Registrar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Sacrificados", style = MaterialTheme.typography.bodySmall)
                    Text("$totalSacrificados", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${filteredRecords.size} registros", style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por cantidad...") },
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
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay registros de sacrificio",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Registre sacrificio usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredRecords, key = { it.id }) { r ->
                        Card(Modifier.fillMaxWidth().clickable { editingRecord = r }) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${r.cantidad} pollos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showDeleteConfirm = r }) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                                }
                                Text("Peso vivo: ${String.format("%.3f", r.pesoVivoPromedio)} kg | Canal: ${String.format("%.3f", r.pesoCanalPromedio)} kg")
                                Text("Merma: ${String.format("%.1f", r.merma)}%", color = MaterialTheme.colorScheme.error)
                                if (r.costoSacrificio != null) Text("Costo: $${String.format("%.2f", r.costoSacrificio)}")
                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(r.fecha)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        SlaughterFormDialog(
            loteId = batchId,
            initial = if (record.id == 0L) null else record,
            onSave = { viewModel.save(it) { editingRecord = null } },
            onDismiss = { editingRecord = null }
        )
    }

    showDeleteConfirm?.let { r -> ConfirmDialog(title = "Eliminar", message = "Eliminar registro de ${r.cantidad} pollos?", onConfirm = { viewModel.delete(r) { showDeleteConfirm = null } }, onDismiss = { showDeleteConfirm = null }) }
}

@Composable
private fun SlaughterFormDialog(loteId: Long, initial: SlaughterEntity? = null, onSave: (SlaughterEntity) -> Unit, onDismiss: () -> Unit) {
    val isEditing = initial != null
    var cantidad by remember { mutableStateOf(initial?.cantidad?.toString() ?: "") }
    var pesoVivo by remember { mutableStateOf(initial?.pesoVivoPromedio?.toString() ?: "") }
    var pesoCanal by remember { mutableStateOf(initial?.pesoCanalPromedio?.toString() ?: "") }
    var costoSac by remember { mutableStateOf(initial?.costoSacrificio?.toString() ?: "") }
    var fecha by remember { mutableStateOf(initial?.fecha ?: System.currentTimeMillis()) }
    var observaciones by remember { mutableStateOf(initial?.observaciones ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var cantError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Sacrificio" else "Registrar Sacrificio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = cantidad, onValueChange = { cantidad = it; cantError = false }, label = { Text("Cantidad *") }, isError = cantError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = pesoVivo, onValueChange = { pesoVivo = it }, label = { Text("Peso Vivo Promedio (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = pesoCanal, onValueChange = { pesoCanal = it }, label = { Text("Peso Canal Promedio (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = costoSac, onValueChange = { costoSac = it }, label = { Text("Costo Sacrificio $") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)), onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (cantidad.isBlank() || cantidad.toIntOrNull() == null || cantidad.toInt() <= 0) { cantError = true; return@TextButton }
                val pVivo = pesoVivo.toDoubleOrNull() ?: 0.0
                val pCanal = pesoCanal.toDoubleOrNull() ?: 0.0
                val merma = if (pVivo > 0) ((pVivo - pCanal) / pVivo * 100) else 0.0
                onSave(SlaughterEntity(
                    id = initial?.id ?: 0L,
                    loteId = loteId, fecha = fecha, cantidad = cantidad.toInt(),
                    pesoVivoPromedio = pVivo, pesoCanalPromedio = pCanal,
                    merma = merma, costoSacrificio = costoSac.toDoubleOrNull(),
                    observaciones = observaciones
                ))
            }) { Text(if (isEditing) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
