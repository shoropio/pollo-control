@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.mortality

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
import com.pollocontrol.app.data.local.entity.MortalityEntity
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import com.pollocontrol.app.ui.components.PolloEmptyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MortalityScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: MortalityViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MortalityViewModel(app) as T
        }
    )

    LaunchedEffect(batchId) {
        viewModel.loadBatch(batchId)
    }

    val lote by viewModel.batch.collectAsState()
    val records by viewModel.mortalityRecords.collectAsState()
    val totalMortality by viewModel.totalMortality.collectAsState()
    val pollosVivos = viewModel.getAliveCount()
    var editingRecord by remember { mutableStateOf<MortalityEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<MortalityEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecords = remember(records, searchQuery) {
        if (searchQuery.isBlank()) records
        else records.filter {
            it.cantidad.toString().contains(searchQuery) ||
            it.motivo.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mortalidad - ${lote?.nombre ?: ""}") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingRecord = MortalityEntity(loteId = batchId, fecha = System.currentTimeMillis(), cantidad = 0) }) { Icon(Icons.Default.Add, "Registrar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalMortality", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("Muertos", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$pollosVivos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Vivos", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pct = if ((lote?.cantidadInicial ?: 0) > 0) (totalMortality.toDouble() / (lote?.cantidadInicial ?: 1) * 100) else 0.0
                        Text("${String.format("%.1f", pct)}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (pct > 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        Text("Mortalidad", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por cantidad o motivo...") },
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
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay registros de mortalidad",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Registre mortalidad usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredRecords, key = { it.id }) { record ->
                        MortalidadCard(record, onClick = { editingRecord = record }, onDelete = { showDeleteConfirm = record })
                    }
                }
            }
        }
    }

    editingRecord?.let { record ->
        MortalityFormDialog(
            loteId = batchId,
            initial = if (record.id == 0L) null else record,
            onSave = { entity -> viewModel.save(entity) { editingRecord = null } },
            onDismiss = { editingRecord = null }
        )
    }

    showDeleteConfirm?.let { record ->
        ConfirmDialog(
            title = "Eliminar registro",
            message = "Eliminar registro de ${record.cantidad} pollos?",
            onConfirm = { viewModel.delete(record) { showDeleteConfirm = null } },
            onDismiss = { showDeleteConfirm = null }
        )
    }
}

@Composable
private fun MortalidadCard(record: MortalityEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val date = remember(record) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(record.fecha)) }
    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${record.cantidad} pollos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (record.motivo.isNotBlank()) Text(record.motivo, style = MaterialTheme.typography.bodyMedium)
                if (record.observaciones.isNotBlank()) Text(record.observaciones, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MortalityFormDialog(loteId: Long, initial: MortalityEntity? = null, onSave: (MortalityEntity) -> Unit, onDismiss: () -> Unit) {
    val isEditing = initial != null
    var cantidad by remember { mutableStateOf(initial?.cantidad?.toString() ?: "") }
    var motivo by remember { mutableStateOf(initial?.motivo ?: "") }
    var fecha by remember { mutableStateOf(initial?.fecha ?: System.currentTimeMillis()) }
    var observaciones by remember { mutableStateOf(initial?.observaciones ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var cantidadError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Mortalidad" else "Registrar Mortalidad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = cantidad, onValueChange = { cantidad = it; cantidadError = false }, label = { Text("Cantidad *") }, isError = cantidadError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)), onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (cantidad.isBlank() || cantidad.toIntOrNull() == null || cantidad.toInt() <= 0) { cantidadError = true; return@TextButton }
                onSave(MortalityEntity(
                    id = initial?.id ?: 0L,
                    loteId = loteId, fecha = fecha, cantidad = cantidad.toInt(),
                    motivo = motivo, observaciones = observaciones
                ))
            }) { Text(if (isEditing) "Actualizar" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
