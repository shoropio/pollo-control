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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pollocontrol.app.data.local.entity.BatchEntity
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.components.PullToSyncBox
import com.pollocontrol.app.ui.settings.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuailBatchesScreen(
    navController: NavHostController
) {
    val viewModel: QuailBatchesViewModel = hiltViewModel()
    val allBatches by viewModel.allBatches.collectAsState(initial = emptyList())
    val batches = remember(allBatches) {
        allBatches.filter { it.especie == "CODORNIZ" }
    }
    val settingsManager = LocalAppDependencies.current.settingsManager
    val firebaseSyncManager = LocalAppDependencies.current.firebaseSyncManager
    val currency by settingsManager.currency.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

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
        }
    ) { padding ->
        PullToSyncBox(
            onSync = { firebaseSyncManager.syncAll() },
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
                    placeholder = { Text("Buscar por nombre, raza o galpon...") },
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
                        subtitle = if (searchQuery.isBlank()) "Cree un lote desde Lotes con especie Codorniz" else "Intente con otros terminos"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { batch ->
                            QuailBatchCard(
                                batch = batch,
                                currencyText = formatMoney(batch.cantidadInicial * batch.precioPorPollito, currency)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuailBatchCard(
    batch: BatchEntity,
    currencyText: String
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
                Text("${batch.raza.ifBlank { "Codorniz" }} · ${batch.galpon.ifBlank { "Sin galpon" }}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("${batch.cantidadInicial} aves · $date", style = MaterialTheme.typography.bodySmall)
                Text(currencyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Surface(color = if (batch.estado == "ACTIVO") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Text(
                    batch.estado.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = if (batch.estado == "ACTIVO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDate(value: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(value))
