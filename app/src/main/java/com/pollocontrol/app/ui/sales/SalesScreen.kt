/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app.ui.sales

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pollocontrol.app.ui.components.ConfirmDialog
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.components.PullToSyncBox
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.ui.settings.formatMoney
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavHostController,
    onNavigateToForm: (Long) -> Unit
) {
    val viewModel: SalesViewModel = hiltViewModel()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val firebaseSyncManager = LocalAppDependencies.current.firebaseSyncManager
    val sales by viewModel.sales.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val currency by settingsManager.currency.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val batchMap = remember(batches) {
        batches.associateBy { it.id }
    }

    val filteredSales = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) sales
        else sales.filter {
            it.tipo.contains(searchQuery, ignoreCase = true) ||
            it.estadoPago.contains(searchQuery, ignoreCase = true)
        }
    }

    var showDeleteConfirm by remember { mutableStateOf<SaleEntity?>(null) }

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("Ventas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Ventas", style = MaterialTheme.typography.bodySmall)
                    Text(formatMoney(totalSales, currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${filteredSales.size} registros", style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por tipo o estado...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true
            )

            if (filteredSales.isEmpty()) {
                PolloEmptyState(
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay ventas registradas",
                    subtitle = if (searchQuery.isNotBlank()) "Intente con otros terminos" else "Agregue una venta usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredSales, key = { it.id }) { venta ->
                        val loteNombre = batchMap[venta.loteId]?.nombre ?: "General"
                        val date = remember(venta) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(venta.fecha)) }
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("${String.format("%.2f", venta.cantidad)} ${if (venta.modalidad == "KILO") "kg" else "unid"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${venta.tipo.lowercase().replaceFirstChar { it.uppercase() }} - ${venta.modalidad}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Lote: $loteNombre", style = MaterialTheme.typography.bodySmall)
                                    Surface(color = when(venta.estadoPago) { "PAGADO" -> Color(0xFFE8F5E9) "PENDIENTE" -> Color(0xFFFFF3E0) else -> Color(0xFFE3F2FD) }, shape = MaterialTheme.shapes.small) {
                                        Text(venta.estadoPago, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatMoney(venta.total, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { showDeleteConfirm = venta }) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    showDeleteConfirm?.let { venta ->
        ConfirmDialog(
            title = "Eliminar Venta",
            message = "Esta accion no se puede deshacer.",
            onConfirm = { viewModel.delete(venta) { showDeleteConfirm = null } },
            onDismiss = { showDeleteConfirm = null }
        )
    }
}
