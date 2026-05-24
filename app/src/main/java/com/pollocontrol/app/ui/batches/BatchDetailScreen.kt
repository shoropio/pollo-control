@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.batches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.pollocontrol.app.ui.mortality.MortalityViewModel
import com.pollocontrol.app.ui.settings.formatMoney
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BatchDetailScreen(
    batchId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit,
    onNavigateToMortality: () -> Unit,
    onNavigateToFeeding: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToWeighing: () -> Unit,
    onNavigateToSlaughter: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val batchViewModel: BatchViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BatchViewModel(app) as T
        }
    )
    val mortalityVM: MortalityViewModel = viewModel(
        key = "mortality_detail",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MortalityViewModel(app) as T
        }
    )

    var batch by remember { mutableStateOf<com.pollocontrol.app.data.local.entity.BatchEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(batchId) {
        batchViewModel.getById(batchId) { result ->
            batch = result
            if (result != null) mortalityVM.loadBatch(batchId)
            isLoading = false
        }
    }

    val mortalidadTotal by mortalityVM.totalMortality.collectAsState()
    val currency by app.settingsManager.currency.collectAsState()
    val pollosVivos = (batch?.cantidadInicial ?: 0) - mortalidadTotal
    val edad = if ((batch?.fechaIngreso ?: 0) > 0) ((System.currentTimeMillis() - (batch?.fechaIngreso ?: 0)) / (1000 * 60 * 60 * 24)).toInt() else 0
    val date = remember(batch) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(batch?.fechaIngreso ?: 0)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.nombre?.ifBlank { "Detalle" } ?: "Detalle") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    IconButton(onClick = onNavigateToEdit) { Icon(Icons.Default.Edit, "Editar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface, actionIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info cards
                Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Edad", "$edad días")
                    DetailRow("Tipo", batch?.let { batchTypeLabel(it) } ?: "-")
                    DetailRow("Raza", batch?.raza?.ifBlank { "-" } ?: "-")
                    DetailRow("Galpón", batch?.galpon?.ifBlank { "-" } ?: "-")
                    DetailRow("Proveedor", batch?.proveedor?.ifBlank { "-" } ?: "-")
                    DetailRow("Fecha Ingreso", date)
                    DetailRow("Estado", batch?.estado?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                }}

                Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Producción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DetailRow("Aves iniciales", "${batch?.cantidadInicial ?: 0}")
                    DetailRow("Mortalidad", "$mortalidadTotal")
                    DetailRow("Aves vivas", "$pollosVivos", if (pollosVivos > 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
                    DetailRow("Costo/Ave", formatMoney(batch?.precioPorPollito ?: 0.0, currency))
                }}

                Text("Gestión del Lote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                ActionButton("Mortalidad y Descartes", Icons.Default.Warning, Color(0xFFD32F2F), onNavigateToMortality)
                ActionButton("Alimentación", Icons.Default.Restaurant, Color(0xFF388E3C), onNavigateToFeeding)
                ActionButton("Sanidad y Medicamentos", Icons.Default.Medication, Color(0xFFEF6C00), onNavigateToHealth)
                ActionButton("Pesajes y Crecimiento", Icons.Default.Scale, Color(0xFF1976D2), onNavigateToWeighing)
                ActionButton("Sacrificio / Beneficiado", Icons.Default.ContentCut, Color(0xFF7B1FA2), onNavigateToSlaughter)

                if (batch?.observaciones?.isNotBlank() == true) {
                    Card { Column(Modifier.padding(16.dp)) {
                        Text("Observaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(batch?.observaciones ?: "", style = MaterialTheme.typography.bodyMedium)
                    }}
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
private fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(shape = androidx.compose.ui.graphics.RectangleShape, onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = color)) {
        Icon(icon, text, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}

private fun batchTypeLabel(batch: com.pollocontrol.app.data.local.entity.BatchEntity): String = when {
    batch.especie == "CODORNIZ" -> "Codorniz"
    batch.especie == "GALLINA" && batch.proposito == "HUEVOS" -> "Gallina ponedora"
    batch.proposito == "HUEVOS" -> "Huevos"
    else -> "Pollo de engorde"
}
