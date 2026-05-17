@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.health

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.TreatmentEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import com.pollocontrol.app.ui.components.PolloEmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HealthScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: HealthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HealthViewModel(app) as T
        }
    )

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val lote by viewModel.batch.collectAsState()
    val treatments by viewModel.treatments.collectAsState()
    var editingRecord by remember { mutableStateOf<TreatmentEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TreatmentEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTreatments = remember(treatments, searchQuery) {
        if (searchQuery.isBlank()) treatments
        else treatments.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            it.tipo.contains(searchQuery, ignoreCase = true) ||
            it.responsable.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sanidad - ${lote?.nombre ?: ""}") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingRecord = TreatmentEntity(loteId = batchId, tipo = "", nombre = "", fechaAplicacion = System.currentTimeMillis()) }) { Icon(Icons.Default.Add, "Registrar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre, tipo o responsable...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )

            if (filteredTreatments.isEmpty()) {
                PolloEmptyState(
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay tratamientos registrados",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Registre un tratamiento usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredTreatments, key = { it.id }) { t ->
                        Card(Modifier.fillMaxWidth().clickable { editingRecord = t }) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(t.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(t.tipo.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showDeleteConfirm = t }) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Dosis: ${t.dosis.ifBlank { "-" }} | Responsable: ${t.responsable.ifBlank { "-" }}")
                                Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(t.fechaAplicacion))}")
                                if (t.costo != null) Text("Costo: $${String.format("%.2f", t.costo)}")
                                if (t.periodoRetiro != null) {
                                    val retiroDate = t.fechaAplicacion + (t.periodoRetiro * 86400000L)
                                    val enRetiro = retiroDate > System.currentTimeMillis()
                                    Text("Retiro: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(retiroDate))}", color = if (enRetiro) Color(0xFFEF6C00) else MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        HealthFormDialog(
            loteId = batchId,
            initial = if (record.id == 0L) null else record,
            onSave = { viewModel.save(it) { editingRecord = null } },
            onDismiss = { editingRecord = null }
        )
    }

    showDeleteConfirm?.let { t ->
        ConfirmDialog(title = "Eliminar tratamiento", message = "Eliminar ${t.nombre}?", onConfirm = { viewModel.delete(t) { showDeleteConfirm = null } }, onDismiss = { showDeleteConfirm = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthFormDialog(loteId: Long, initial: TreatmentEntity? = null, onSave: (TreatmentEntity) -> Unit, onDismiss: () -> Unit) {
    val isEditing = initial != null
    var tipo by remember { mutableStateOf(initial?.tipo ?: "MEDICAMENTO") }
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var dosis by remember { mutableStateOf(initial?.dosis ?: "") }
    var responsable by remember { mutableStateOf(initial?.responsable ?: "") }
    var costo by remember { mutableStateOf(initial?.costo?.toString() ?: "") }
    var periodoRetiro by remember { mutableStateOf(initial?.periodoRetiro?.toString() ?: "") }
    var fecha by remember { mutableStateOf(initial?.fechaAplicacion ?: System.currentTimeMillis()) }
    var observaciones by remember { mutableStateOf(initial?.observaciones ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    val tipos = listOf("VACUNA", "MEDICAMENTO", "VITAMINA", "OTRO")
    var tipoExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Tratamiento" else "Nuevo Tratamiento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = !tipoExpanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                        tipos.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { tipo = t; tipoExpanded = false }) }
                    }
                }
                OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = false }, label = { Text("Nombre *") }, isError = nombreError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dosis, onValueChange = { dosis = it }, label = { Text("Dosis") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = responsable, onValueChange = { responsable = it }, label = { Text("Responsable") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = costo, onValueChange = { costo = it }, label = { Text("Costo $") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = periodoRetiro, onValueChange = { periodoRetiro = it }, label = { Text("Periodo de Retiro (dias)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)), onValueChange = {}, label = { Text("Fecha Aplicacion") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isBlank()) { nombreError = true; return@TextButton }
                onSave(TreatmentEntity(
                    id = initial?.id ?: 0L,
                    loteId = loteId, tipo = tipo, nombre = nombre,
                    fechaAplicacion = fecha, dosis = dosis, responsable = responsable,
                    costo = costo.toDoubleOrNull(),
                    periodoRetiro = periodoRetiro.toIntOrNull(),
                    observaciones = observaciones
                ))
            }) { Text(if (isEditing) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
