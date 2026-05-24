@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.expenses

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
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun ExpenseFormScreen(
    gastoId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: ExpensesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ExpensesViewModel(app) as T
        }
    )
    val isEditing = gastoId != 0L
    val batchMap by viewModel.batchMap.collectAsState()
    val currency by app.settingsManager.currency.collectAsState()

    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("OTRO") }
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(System.currentTimeMillis()) }
    var loteId by remember { mutableStateOf<Long?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }
    var montoError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    val tipos = listOf("POLLITOS", "ALIMENTO", "MEDICAMENTOS", "MANO_OBRA", "TRANSPORTE", "ELECTRICIDAD", "AGUA", "GAS", "MANTENIMIENTO", "SACRIFICIO", "EMPAQUE", "OTRO")
    var tipoExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(gastoId) {
        if (isEditing) {
            viewModel.getById(gastoId) { g ->
                if (g != null) {
                    descripcion = g.descripcion; tipo = g.tipo; monto = g.monto.toString()
                    fecha = g.fecha; loteId = g.loteId; observaciones = g.observaciones
                }
                isLoading = false
            }
        } else isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Gasto" else "Nuevo Gasto") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it; descError = false }, label = { Text("Descripción *") }, isError = descError, modifier = Modifier.fillMaxWidth(), singleLine = true)

                ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = !tipoExpanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                        tipos.forEach { t -> DropdownMenuItem(text = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { tipo = t; tipoExpanded = false }) }
                    }
                }

                OutlinedTextField(value = monto, onValueChange = { monto = it; montoError = false }, label = { Text("Monto *") }, isError = montoError, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })

                val dateStr = remember(fecha) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)) }
                OutlinedTextField(value = dateStr, onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })

                // Lote selector (simplified - just a dropdown)
                var loteExpanded by remember { mutableStateOf(false) }
                var loteLabel by remember { mutableStateOf("General") }
                ExposedDropdownMenuBox(expanded = loteExpanded, onExpandedChange = { loteExpanded = !loteExpanded }) {
                    OutlinedTextField(value = loteLabel, onValueChange = {}, readOnly = true, label = { Text("Asignar a Lote") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(loteExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = loteExpanded, onDismissRequest = { loteExpanded = false }) {
                        DropdownMenuItem(text = { Text("General (sin lote)") }, onClick = { loteId = null; loteLabel = "General"; loteExpanded = false })
                        batchMap.forEach { (id, name) ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { loteId = id; loteLabel = name; loteExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                Spacer(Modifier.height(8.dp))
                Button(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click@{
                    var valid = true
                    if (descripcion.isBlank()) { descError = true; valid = false }
                    if (monto.isBlank() || monto.toDoubleOrNull() == null || monto.toDouble() <= 0) { montoError = true; valid = false }
                    if (!valid) return@click

                    viewModel.save(ExpenseEntity(
                        id = if (isEditing) gastoId else 0L,
                        loteId = loteId, tipo = tipo, descripcion = descripcion,
                        monto = monto.toDouble(), fecha = fecha, observaciones = observaciones
                    )) { onNavigateBack() }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }

                if (isEditing) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    OutlinedButton(
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar")
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar", fontWeight = FontWeight.Bold)
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Eliminar Gasto") },
                            text = { Text("Esta accion no se puede deshacer.") },
                            confirmButton = {
                                TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = {
                                    viewModel.delete(ExpenseEntity(id = gastoId, loteId = null, tipo = "", descripcion = "", monto = 0.0, fecha = 0L, observaciones = "")) { onNavigateBack() }
                                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = { TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
                        )
                    }
                }
            }
        }
    }
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
