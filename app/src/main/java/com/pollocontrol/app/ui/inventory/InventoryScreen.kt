/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.inventory

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
import com.pollocontrol.app.data.local.entity.SupplyEntity
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R

@Composable
fun InventoryScreen(
    navController: NavHostController,
    onNavigateToForm: (Long) -> Unit
) {
    val viewModel: InventoryViewModel = hiltViewModel()
    val firebaseSyncManager = LocalAppDependencies.current.firebaseSyncManager
    val insumos by viewModel.filteredSupplies.collectAsState()
    val bajoStock by viewModel.lowStockSupplies.collectAsState()
    var filter by remember { mutableStateOf("TODOS") }
    var searchQuery by remember { mutableStateOf("") }
    val tipos = listOf("TODOS", "ALIMENTO", "MEDICAMENTO", "VITAMINA", "VACUNA", "DESINFECTANTE", "GAS", "CAMA", "EMPAQUE", "OTRO")

    val filteredAndSearched = remember(insumos, searchQuery) {
        if (searchQuery.isBlank()) insumos
        else insumos.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            it.tipo.contains(searchQuery, ignoreCase = true) ||
            it.unidad.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventario)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { onNavigateToForm(0L) }) { Icon(Icons.Default.Add, "Agregar") }
        }
    ) { padding ->
        PullToSyncBox(
            onSync = { firebaseSyncManager.syncAll() },
            modifier = Modifier.padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
            if (bajoStock.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, "Alerta", tint = Color(0xFFEF6C00))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.insumos_stock_bajo, bajoStock.size), fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                    }
                }
            }

            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tipos.forEach { t ->
                    FilterChip(selected = filter == t, onClick = { filter = t; viewModel.setFilter(t) }, label = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.buscar_inventario_placeholder)) },
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
                    title = if (searchQuery.isNotBlank()) stringResource(R.string.sin_resultados) else stringResource(R.string.no_hay_insumos),
                    subtitle = if (searchQuery.isNotBlank()) stringResource(R.string.intente_otros_terminos) else stringResource(R.string.agregue_insumo_boton)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredAndSearched, key = { it.id }) { insumo ->
                        InsumoCard(insumo, onClick = { onNavigateToForm(insumo.id) })
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun InsumoCard(insumo: SupplyEntity, onClick: () -> Unit) {
    val isLow = insumo.stockActual <= insumo.stockMinimo
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(insumo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(insumo.tipo.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.stock_formato, insumo.stockActual.toString(), insumo.unidad), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.minimo_formato, insumo.stockMinimo.toString(), insumo.unidad), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isLow) {
                Icon(Icons.Default.Warning, "Stock bajo", tint = Color(0xFFEF6C00))
            }
        }
    }
}
