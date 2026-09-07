/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.batches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.components.PullToSyncBox
import com.pollocontrol.app.data.local.entity.BatchEntity
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BatchesScreen(
    navController: NavHostController,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToForm: (Long) -> Unit
) {
    val viewModel: BatchViewModel = hiltViewModel()
    val firebaseSyncManager = LocalAppDependencies.current.firebaseSyncManager
    val batches by viewModel.filteredBatches.collectAsState()
    var filter by remember { mutableStateOf("TODOS") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("TODOS", "ACTIVO", "FINALIZADO", "VENDIDO", "SACRIFICADO")

    val filteredAndSearched = remember(batches, searchQuery) {
        if (searchQuery.isBlank()) batches
        else batches.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            batchTypeLabel(it).contains(searchQuery, ignoreCase = true) ||
            it.raza.contains(searchQuery, ignoreCase = true) ||
            it.galpon.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lotes)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { onNavigateToForm(0L) }) {
                Icon(Icons.Default.Add, stringResource(R.string.agregar_lote))
            }
        }
    ) { padding ->
        PullToSyncBox(
            onSync = { firebaseSyncManager.syncAll() },
            modifier = Modifier.padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f; viewModel.setFilter(f) },
                        label = { Text(f.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.buscar_lotes_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true
            )

            if (filteredAndSearched.isEmpty()) {
                PolloEmptyState(
                    title = if (searchQuery.isNotBlank()) stringResource(R.string.sin_resultados) else stringResource(R.string.no_hay_lotes),
                    subtitle = if (searchQuery.isNotBlank()) stringResource(R.string.intente_otros_terminos) else stringResource(R.string.agregue_lote_boton)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filteredAndSearched, key = { it.id }) { batch ->
            BatchCard(batch, onClick = { onNavigateToDetail(batch.id) })
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun BatchCard(batch: BatchEntity, onClick: () -> Unit) {
    val edad = ((System.currentTimeMillis() - batch.fechaIngreso) / (1000 * 60 * 60 * 24)).toInt()
    val date = remember(batch) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(batch.fechaIngreso)) }
    val estadoColor = when (batch.estado) {
        "ACTIVO" -> MaterialTheme.colorScheme.primary
        "FINALIZADO" -> MaterialTheme.colorScheme.tertiary
        "VENDIDO" -> Color(0xFF1976D2)
        "SACRIFICADO" -> Color(0xFF7B1FA2)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(batch.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(color = estadoColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        batch.estado.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = estadoColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                batchTypeLabel(batch),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(stringResource(R.string.edad), stringResource(R.string.edad_dias_formato, edad))
                InfoItem(stringResource(R.string.aves_card), "${batch.cantidadInicial}")
                InfoItem(stringResource(R.string.galpon_card), batch.galpon.ifBlank { "-" })
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(stringResource(R.string.raza_card), batch.raza.ifBlank { "-" })
                InfoItem(stringResource(R.string.ingreso_card), date)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun batchTypeLabel(batch: BatchEntity): String = when {
    batch.especie == "CODORNIZ" -> "Codorniz"
    batch.especie == "GALLINA" && batch.proposito == "HUEVOS" -> "Gallina ponedora"
    batch.proposito == "HUEVOS" -> "Huevos"
    else -> "Pollo de engorde"
}
