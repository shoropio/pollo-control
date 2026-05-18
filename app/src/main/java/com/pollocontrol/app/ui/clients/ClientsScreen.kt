@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.clients

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.ClientEntity
import com.pollocontrol.app.data.settings.AppCurrency
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.settings.formatMoney

@Composable
fun ClientsScreen(
    app: PolloControlApp,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long) -> Unit
) {
    val viewModel: ClientsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ClientsViewModel(app) as T
        }
    )
    val searchResults by viewModel.searchResults.collectAsState()
    val currency by app.settingsManager.currency.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(0L) }) { Icon(Icons.Default.Add, "Agregar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.search(it) },
                placeholder = { Text("Buscar por nombre o telefono...") },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = { if (query.isNotEmpty()) { IconButton(onClick = { query = ""; viewModel.search("") }) { Icon(Icons.Default.Clear, "Limpiar") } } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = MaterialTheme.shapes.medium
            )

            if (searchResults.isEmpty()) {
                PolloEmptyState(
                    title = if (query.isNotBlank()) "Sin resultados" else "No hay clientes registrados",
                    subtitle = if (query.isNotBlank()) "Intente con otros terminos" else "Agregue un cliente usando el boton +"
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults, key = { it.id }) { cliente ->
                        ClienteCard(cliente, currency, onClick = { onNavigateToForm(cliente.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ClienteCard(cliente: ClientEntity, currency: AppCurrency, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(cliente.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (cliente.telefono.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, "Telefono", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(cliente.telefono, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (cliente.direccion.isNotBlank()) {
                    Text(cliente.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (cliente.saldoPendiente > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Saldo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMoney(cliente.saldoPendiente, currency), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            }
        }
    }
}
