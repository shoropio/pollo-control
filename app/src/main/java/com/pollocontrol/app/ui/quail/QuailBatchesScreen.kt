package com.pollocontrol.app.ui.quail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.QuailBatchEntity
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.components.PullToSyncBox
import com.pollocontrol.app.ui.settings.formatMoney
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuailBatchesScreen(
    app: PolloControlApp,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val batches by app.quailBatchRepository.getAll().collectAsState(initial = emptyList())
    val currency by app.settingsManager.currency.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = remember(batches, searchQuery) {
        if (searchQuery.isBlank()) batches else batches.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.raza.contains(searchQuery, ignoreCase = true) ||
                it.galpon.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("Codornices") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        PullToSyncBox(
            onSync = { app.firebaseSyncManager.syncAll() },
            modifier = Modifier.padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Lotes de codorniz", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${batches.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${batches.sumOf { it.cantidadInicial }} aves registradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, raza o galpón...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                if (filtered.isEmpty()) {
                    PolloEmptyState(
                        title = if (searchQuery.isBlank()) "No hay lotes de codorniz" else "Sin resultados",
                        subtitle = if (searchQuery.isBlank()) "Agregue un lote usando el botón +" else "Intente con otros términos"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { batch ->
                            QuailBatchCard(
                                batch = batch,
                                currencyText = formatMoney(batch.cantidadInicial * batch.precioUnitario, currency),
                                onDelete = {
                                    scope.launch { app.quailBatchRepository.delete(batch) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        QuailBatchDialog(
            onDismiss = { showDialog = false },
            onSave = { batch ->
                scope.launch { app.quailBatchRepository.insert(batch) }
                showDialog = false
            }
        )
    }
}

@Composable
private fun QuailBatchCard(
    batch: QuailBatchEntity,
    currencyText: String,
    onDelete: () -> Unit
) {
    val date = remember(batch.fechaIngreso) { formatDate(batch.fechaIngreso) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(batch.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${batch.raza.ifBlank { "Codorniz" }} · ${batch.galpon.ifBlank { "Sin galpón" }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("${batch.cantidadInicial} aves · $date", style = MaterialTheme.typography.bodySmall)
                Text(currencyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "Eliminar",
                modifier = Modifier.clickable(onClick = onDelete),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuailBatchDialog(
    onDismiss: () -> Unit,
    onSave: (QuailBatchEntity) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var galpon by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo lote de codorniz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad inicial") }, singleLine = true)
                OutlinedTextField(precio, { precio = it }, label = { Text("Precio unitario") }, singleLine = true)
                OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, singleLine = true)
                OutlinedTextField(galpon, { galpon = it }, label = { Text("Galpón") }, singleLine = true)
                OutlinedTextField(proveedor, { proveedor = it }, label = { Text("Proveedor") }, singleLine = true)
                OutlinedTextField(observaciones, { observaciones = it }, label = { Text("Observaciones") })
            }
        },
        confirmButton = {
            Button(
                shape = androidx.compose.ui.graphics.RectangleShape,
                enabled = nombre.isNotBlank() && cantidad.toIntOrNull() != null,
                onClick = {
                    onSave(
                        QuailBatchEntity(
                            nombre = nombre.trim(),
                            fechaIngreso = System.currentTimeMillis(),
                            cantidadInicial = cantidad.toIntOrNull() ?: 0,
                            precioUnitario = precio.toDoubleOrNull() ?: 0.0,
                            raza = raza.trim(),
                            galpon = galpon.trim(),
                            proveedor = proveedor.trim(),
                            observaciones = observaciones.trim()
                        )
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(value))
