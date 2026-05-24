@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.feeding

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
import com.pollocontrol.app.data.local.entity.FeedingEntity
import com.pollocontrol.app.data.settings.AppCurrency
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.settings.formatMoney
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedingScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: FeedingViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FeedingViewModel(app) as T
        }
    )

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val lote by viewModel.batch.collectAsState()
    val records by viewModel.records.collectAsState()
    val totalConsumo by viewModel.totalConsumption.collectAsState()
    val currency by app.settingsManager.currency.collectAsState()
    var editingRecord by remember { mutableStateOf<FeedingEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<FeedingEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter {
            it.tipo.contains(searchQuery, ignoreCase = true) ||
            it.cantidadKg.toString().contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alimentacion - ${lote?.nombre ?: ""}") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { editingRecord = FeedingEntity(loteId = batchId, fecha = System.currentTimeMillis(), tipo = "", cantidadKg = 0.0) }) { Icon(Icons.Default.Add, "Registrar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Consumo Total: ${String.format("%.2f", totalConsumo)} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Registros: ${records.size}", style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por tipo o cantidad...") },
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
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay registros de alimentacion",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Registre alimentacion usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredRecords, key = { it.id }) { r ->
                        Card(Modifier.fillMaxWidth().clickable { editingRecord = r }) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(r.tipo.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { showDeleteConfirm = r }) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                                }
                                Text("${String.format("%.2f", r.cantidadKg)} kg", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (r.costo != null) Text("Costo: ${formatMoney(r.costo, currency)}", style = MaterialTheme.typography.bodyMedium)
                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(r.fecha)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        FeedingFormDialog(
            loteId = batchId,
            initial = if (record.id == 0L) null else record,
            currency = currency,
            onSave = { viewModel.save(it) { editingRecord = null } },
            onDismiss = { editingRecord = null }
        )
    }

    showDeleteConfirm?.let { r ->
        ConfirmDialog(title = "Eliminar registro", message = "Eliminar registro de alimentacion?", onConfirm = { viewModel.delete(r) { showDeleteConfirm = null } }, onDismiss = { showDeleteConfirm = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedingFormDialog(loteId: Long, initial: FeedingEntity? = null, currency: AppCurrency, onSave: (FeedingEntity) -> Unit, onDismiss: () -> Unit) {
    val isEditing = initial != null
    var tipo by remember { mutableStateOf(initial?.tipo ?: "INICIO") }
    var cantidadKg by remember { mutableStateOf(initial?.cantidadKg?.toString() ?: "") }
    var cantidadSacos by remember { mutableStateOf(initial?.cantidadSacos?.toString() ?: "") }
    var costo by remember { mutableStateOf(initial?.costo?.toString() ?: "") }
    var fecha by remember { mutableStateOf(initial?.fecha ?: System.currentTimeMillis()) }
    var observaciones by remember { mutableStateOf(initial?.observaciones ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var cantidadError by remember { mutableStateOf(false) }
    val tipos = listOf("INICIO", "CRECIMIENTO", "ENGORDE", "FINALIZADOR")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Alimentacion" else "Registrar Alimentacion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tipos.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { tipo = t; expanded = false }) }
                    }
                }
                OutlinedTextField(value = cantidadKg, onValueChange = { cantidadKg = it; cantidadError = false }, label = { Text("Cantidad (kg) *") }, isError = cantidadError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = cantidadSacos, onValueChange = { cantidadSacos = it }, label = { Text("Sacos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = costo, onValueChange = { costo = it }, label = { Text("Costo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })
                OutlinedTextField(value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)), onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = {
                if (cantidadKg.isBlank() || cantidadKg.toDoubleOrNull() == null) { cantidadError = true; return@TextButton }
                onSave(FeedingEntity(
                    id = initial?.id ?: 0L,
                    loteId = loteId, fecha = fecha, tipo = tipo,
                    cantidadKg = cantidadKg.toDouble(),
                    cantidadSacos = cantidadSacos.toIntOrNull(),
                    costo = costo.toDoubleOrNull(),
                    observaciones = observaciones
                ))
            }) { Text(if (isEditing) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onDismiss) { Text("Cancelar") } }
    )
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
