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
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.data.local.entity.SupplyEntity
import com.pollocontrol.app.ui.components.LocalAppDependencies
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R
@Composable
fun InventoryFormScreen(
    insumoId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: InventoryViewModel = hiltViewModel()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()
    val isEditing = insumoId != 0L

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
                title = { Text(if (isEditing) stringResource(R.string.editar_insumo) else stringResource(R.string.nuevo_insumo)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = false }, label = { Text(stringResource(R.string.nombre_label)) }, isError = nombreError, modifier = Modifier.fillMaxWidth(), singleLine = true)

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.tipo)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tipos.forEach { t -> DropdownMenuItem(text = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { tipo = t; expanded = false }) }
                    }
                }
                OutlinedTextField(value = stockActual, onValueChange = { stockActual = it }, label = { Text(stringResource(R.string.stock_actual_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = stockMinimo, onValueChange = { stockMinimo = it }, label = { Text(stringResource(R.string.stock_minimo_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text(stringResource(R.string.unidad_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text(stringResource(R.string.observaciones)) }, modifier = Modifier.fillMaxWidth(), minLines = 2)

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
                    Text(stringResource(R.string.guardar), fontWeight = FontWeight.Bold)
                }

                if (isEditing) {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    OutlinedButton(
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, stringResource(R.string.eliminar))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.eliminar), fontWeight = FontWeight.Bold)
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text(stringResource(R.string.eliminar_insumo)) },
                            text = { Text(stringResource(R.string.accion_no_deshacer)) },
                            confirmButton = {
                                TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = {
                                    viewModel.delete(SupplyEntity(id = insumoId, nombre = "", tipo = "", stockActual = 0.0, stockMinimo = 0.0, unidad = "", observaciones = "")) { onNavigateBack() }
                                }) { Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = { TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancelar)) } }
                        )
                    }
                }

                // Stock movement section
                if (isEditing) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(stringResource(R.string.movimiento_stock), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    var movCantidad by remember { mutableStateOf("") }
                    var movCosto by remember { mutableStateOf("") }
                    var movObs by remember { mutableStateOf("") }

                    OutlinedTextField(value = movCantidad, onValueChange = { movCantidad = it }, label = { Text(stringResource(R.string.cantidad_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = movCosto, onValueChange = { movCosto = it }, label = { Text(stringResource(R.string.costo_solo_entrada)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })
                    OutlinedTextField(value = movObs, onValueChange = { movObs = it }, label = { Text(stringResource(R.string.observaciones)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click1@{
                            val cant = movCantidad.toDoubleOrNull() ?: return@click1
                            viewModel.addStock(insumoId, cant, movCosto.toDoubleOrNull(), movObs)
                            movCantidad = ""; movCosto = ""; movObs = ""
                        }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, stringResource(R.string.entrada)); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.entrada)) }

                        OutlinedButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = click2@{
                            val cant = movCantidad.toDoubleOrNull() ?: return@click2
                            viewModel.removeStock(insumoId, cant, movObs)
                            movCantidad = ""; movCosto = ""; movObs = ""
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Remove, stringResource(R.string.salida)); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.salida))
                        }
                    }
                }
            }
        }
    }
}
