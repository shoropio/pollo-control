@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.expenses

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.data.local.entity.ExpenseEntity
import com.pollocontrol.app.data.settings.AppCurrency
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.components.PolloEmptyState
import com.pollocontrol.app.ui.settings.formatMoney
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long) -> Unit
) {
    val viewModel: ExpensesViewModel = hiltViewModel()
    val expenses by viewModel.expenses.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val batchMap by viewModel.batchMap.collectAsState()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filteredExpenses = remember(expenses, searchQuery) {
        if (searchQuery.isBlank()) expenses
        else expenses.filter {
            it.descripcion.contains(searchQuery, ignoreCase = true) ||
            it.tipo.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gastos)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = { onNavigateToForm(0L) }) { Icon(Icons.Default.Add, "Agregar") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.total_gastos), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMoney(totalExpenses, currency), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.registros_formato, filteredExpenses.size), style = MaterialTheme.typography.bodySmall)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.buscar_gastos_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Limpiar") }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true
            )

            if (filteredExpenses.isEmpty()) {
                PolloEmptyState(
                    title = if (searchQuery.isNotBlank()) stringResource(R.string.sin_resultados) else stringResource(R.string.no_hay_gastos),
                    subtitle = if (searchQuery.isNotBlank()) stringResource(R.string.intente_otros_terminos) else stringResource(R.string.agregue_gasto_boton)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredExpenses, key = { it.id }) { gasto ->
                        GastoCard(gasto, batchMap, currency, onClick = { onNavigateToForm(gasto.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GastoCard(gasto: ExpenseEntity, batchMap: Map<Long, String>, currency: AppCurrency, onClick: () -> Unit) {
    val date = remember(gasto) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(gasto.fecha)) }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(gasto.descripcion, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(gasto.tipo.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (gasto.loteId != null && batchMap.containsKey(gasto.loteId)) {
                    Text(stringResource(R.string.lote_formato, batchMap[gasto.loteId] ?: ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (gasto.loteId == null) {
                    Text(stringResource(R.string.gasto_general), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(formatMoney(gasto.monto, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        }
    }
}
