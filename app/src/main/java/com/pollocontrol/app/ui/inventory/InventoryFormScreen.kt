@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.inventory

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
import com.pollocontrol.app.data.local.entity.SupplyEntity
@Composable
fun InventoryFormScreen(
    insumoId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: InventoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = InventoryViewModel(app) as T
        }
    )
    val isEditing = insumoId != 0L
    val currency by app.settingsManager.currency.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("OTRO") }
    var stockActual by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    val tipos = listOf("ALIMENTO", "MEDICAMENTO", "VITAMINA", "VACUNA", "DESINFECTANTE", "GAS", "CAMA", "EMPAQUE", "OTRO")
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(insumoId) {
        if (isEditing) {
            viewModel.getById(insumoId) { insumo ->
                if (insumo != null) {
                    nombre = insumo.nombre; tipo = insumo.tipo; stockActual = insumo.stockActual.toString()
                    stockMinimo = insumo.stockMinimo.toString(); unidad = insumo.unidad; observaciones = insumo.observaciones
                }
                isLoading = false
            }
        } else isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Insumo" else "Nuevo Insumo") },
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

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tipos.forEach { t -> DropdownMenuItem(text = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { tipo = t; expanded = false }) }
                    }
                }
                OutlinedTextField(value = stockActual, onValueChange = { stockActual = it }, label = { Text("Stock Actual") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = stockMinimo, onValueChange = { stockMinimo = it }, label = { Text("Stock Mínimo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad (kg, sacos, lt, etc)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                Spacer(Modifier.height(8.dp))
                Button(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click@{
                    if (nombre.isBlank()) { nombreError = true; return@click }
                    viewModel.save(SupplyEntity(
                        id = if (isEditing) insumoId else 0L,
                        nombre = nombre, tipo = tipo,
                        stockActual = stockActual.toDoubleOrNull() ?: 0.0,
                        stockMinimo = stockMinimo.toDoubleOrNull() ?: 0.0,
                        unidad = unidad, observaciones = observaciones
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
                            title = { Text("Eliminar Insumo") },
                            text = { Text("Esta accion no se puede deshacer.") },
                            confirmButton = {
                                TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = {
                                    viewModel.delete(SupplyEntity(id = insumoId, nombre = "", tipo = "", stockActual = 0.0, stockMinimo = 0.0, unidad = "", observaciones = "")) { onNavigateBack() }
                                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = { TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
                        )
                    }
                }

                // Stock movement section
                if (isEditing) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Movimiento de Stock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    var movCantidad by remember { mutableStateOf("") }
                    var movCosto by remember { mutableStateOf("") }
                    var movObs by remember { mutableStateOf("") }

                    OutlinedTextField(value = movCantidad, onValueChange = { movCantidad = it }, label = { Text("Cantidad") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = movCosto, onValueChange = { movCosto = it }, label = { Text("Costo (solo entrada)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })
                    OutlinedTextField(value = movObs, onValueChange = { movObs = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click1@{
                            val cant = movCantidad.toDoubleOrNull() ?: return@click1
                            viewModel.addStock(insumoId, cant, movCosto.toDoubleOrNull(), movObs)
                            movCantidad = ""; movCosto = ""; movObs = ""
                        }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, "Entrada"); Spacer(Modifier.width(4.dp)); Text("Entrada") }

                        OutlinedButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click2@{
                            val cant = movCantidad.toDoubleOrNull() ?: return@click2
                            viewModel.removeStock(insumoId, cant, movObs)
                            movCantidad = ""; movCosto = ""; movObs = ""
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Remove, "Salida"); Spacer(Modifier.width(4.dp)); Text("Salida")
                        }
                    }
                }
            }
        }
    }
}
