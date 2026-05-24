package com.pollocontrol.app.ui.eggs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.data.local.entity.EggProductionEntity
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.components.PullToSyncBox
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggProductionScreen(
    app: PolloControlApp,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val allBatches by app.batchRepository.getAll().collectAsState(initial = emptyList())
    val batches = remember(allBatches) {
        allBatches.filter { it.proposito == "HUEVOS" && (it.especie == "GALLINA" || it.especie == "CODORNIZ") }
    }
    val allEggs by app.eggProductionRepository.getAll().collectAsState(initial = emptyList())
    var selectedBatchId by remember { mutableLongStateOf(0L) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(batches) {
        if (selectedBatchId == 0L && batches.isNotEmpty()) {
            selectedBatchId = batches.first().id
        }
    }

    val filteredEggs = remember(allEggs, selectedBatchId) {
        if (selectedBatchId == 0L) allEggs else allEggs.filter { it.loteId == selectedBatchId }
    }
    val batchNames = remember(batches) { batches.associate { it.id to it.nombre } }

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("Huevos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = androidx.compose.ui.graphics.RectangleShape,
                onClick = { if (batches.isNotEmpty()) showDialog = true }
            ) {
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
                        Text("Producción registrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${filteredEggs.sumOf { it.cantidadHuevos }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${filteredEggs.size} registros", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (batches.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        batches.forEach { batch ->
                            FilterChip(
                                selected = selectedBatchId == batch.id,
                                onClick = { selectedBatchId = batch.id },
                                label = { Text(batch.nombre) }
                            )
                        }
                    }
                }

                if (batches.isEmpty()) {
                    PolloEmptyState(
                        title = "Primero registre codornices",
                        subtitle = "La producción de huevos necesita un lote de codorniz"
                    )
                } else if (filteredEggs.isEmpty()) {
                    PolloEmptyState(
                        title = "No hay producción registrada",
                        subtitle = "Agregue producción usando el botón +"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEggs, key = { it.id }) { item ->
                            EggProductionCard(
                                item = item,
                                batchName = batchNames[item.loteId].orEmpty(),
                                onDelete = {
                                    scope.launch { app.eggProductionRepository.delete(item) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        EggProductionDialog(
            batches = batches,
            selectedBatchId = selectedBatchId,
            onDismiss = { showDialog = false },
            onSave = { item ->
                scope.launch { app.eggProductionRepository.insert(item) }
                selectedBatchId = item.loteId
                showDialog = false
            }
        )
    }
}

@Composable
private fun EggProductionCard(
    item: EggProductionEntity,
    batchName: String,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("${item.cantidadHuevos} huevos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(batchName.ifBlank { "Lote de codorniz" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDate(item.fecha), style = MaterialTheme.typography.bodySmall)
                Text("A: ${item.calidadA} · B: ${item.calidadB} · Rechazos: ${item.rechazos}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onDelete) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EggProductionDialog(
    batches: List<BatchEntity>,
    selectedBatchId: Long,
    onDismiss: () -> Unit,
    onSave: (EggProductionEntity) -> Unit
) {
    var batchId by remember { mutableLongStateOf(selectedBatchId.takeIf { it != 0L } ?: batches.first().id) }
    var cantidad by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var calidadA by remember { mutableStateOf("") }
    var calidadB by remember { mutableStateOf("") }
    var rechazos by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva producción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    batches.forEach { batch ->
                        FilterChip(
                            selected = batchId == batch.id,
                            onClick = { batchId = batch.id },
                            label = { Text(batch.nombre) }
                        )
                    }
                }
                OutlinedTextField(cantidad, { cantidad = it }, label = { Text("Cantidad de huevos") }, singleLine = true)
                OutlinedTextField(peso, { peso = it }, label = { Text("Peso promedio") }, singleLine = true)
                OutlinedTextField(calidadA, { calidadA = it }, label = { Text("Calidad A") }, singleLine = true)
                OutlinedTextField(calidadB, { calidadB = it }, label = { Text("Calidad B") }, singleLine = true)
                OutlinedTextField(rechazos, { rechazos = it }, label = { Text("Rechazos") }, singleLine = true)
                OutlinedTextField(observaciones, { observaciones = it }, label = { Text("Observaciones") })
            }
        },
        confirmButton = {
            Button(
                shape = androidx.compose.ui.graphics.RectangleShape,
                enabled = cantidad.toIntOrNull() != null,
                onClick = {
                    onSave(
                        EggProductionEntity(
                            loteId = batchId,
                            fecha = System.currentTimeMillis(),
                            cantidadHuevos = cantidad.toIntOrNull() ?: 0,
                            pesoPromedio = peso.toDoubleOrNull() ?: 0.0,
                            calidadA = calidadA.toIntOrNull() ?: 0,
                            calidadB = calidadB.toIntOrNull() ?: 0,
                            rechazos = rechazos.toIntOrNull() ?: 0,
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
