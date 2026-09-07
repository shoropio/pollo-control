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
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.mortality.MortalityViewModel
import com.pollocontrol.app.ui.settings.formatMoney
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BatchDetailScreen(
    batchId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMortality: () -> Unit,
    onNavigateToFeeding: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToWeighing: () -> Unit,
    onNavigateToSlaughter: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val batchViewModel: BatchViewModel = hiltViewModel()
    val mortalityVM: MortalityViewModel = hiltViewModel()

    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()

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
    val pollosVivos = (batch?.cantidadInicial ?: 0) - mortalidadTotal
    val edad = if ((batch?.fechaIngreso ?: 0) > 0) ((System.currentTimeMillis() - (batch?.fechaIngreso ?: 0)) / (1000 * 60 * 60 * 24)).toInt() else 0
    val date = remember(batch) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(batch?.fechaIngreso ?: 0)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(batch?.nombre?.ifBlank { stringResource(R.string.detalle) } ?: stringResource(R.string.detalle)) },
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
                    DetailRow(stringResource(R.string.edad), stringResource(R.string.edad_dias, edad))
                    DetailRow(stringResource(R.string.tipo), batch?.let { batchTypeLabel(it) } ?: "-")
                    DetailRow(stringResource(R.string.raza_card), batch?.raza?.ifBlank { "-" } ?: "-")
                    DetailRow(stringResource(R.string.galpon), batch?.galpon?.ifBlank { "-" } ?: "-")
                    DetailRow(stringResource(R.string.proveedor), batch?.proveedor?.ifBlank { "-" } ?: "-")
                    DetailRow(stringResource(R.string.fecha_ingreso), date)
                    DetailRow(stringResource(R.string.estado), batch?.estado?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                }}

                Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.produccion), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DetailRow(stringResource(R.string.aves_iniciales), "${batch?.cantidadInicial ?: 0}")
                    DetailRow(stringResource(R.string.mortalidad), "$mortalidadTotal")
                    DetailRow(stringResource(R.string.aves_vivas), "$pollosVivos", if (pollosVivos > 0) Color(0xFF388E3C) else Color(0xFFD32F2F))
                    DetailRow(stringResource(R.string.costo_ave), formatMoney(batch?.precioPorPollito ?: 0.0, currency))
                }}

                Text(stringResource(R.string.gestion_lote), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                ActionButton(stringResource(R.string.mortalidad_descartes), Icons.Default.Warning, Color(0xFFD32F2F), onNavigateToMortality)
                ActionButton(stringResource(R.string.alimentacion), Icons.Default.Restaurant, Color(0xFF388E3C), onNavigateToFeeding)
                ActionButton(stringResource(R.string.sanidad_medicamentos), Icons.Default.Medication, Color(0xFFEF6C00), onNavigateToHealth)
                ActionButton(stringResource(R.string.pesajes_crecimiento), Icons.Default.Scale, Color(0xFF1976D2), onNavigateToWeighing)
                ActionButton(stringResource(R.string.sacrificio_beneficiado), Icons.Default.ContentCut, Color(0xFF7B1FA2), onNavigateToSlaughter)

                if (batch?.observaciones?.isNotBlank() == true) {
                    Card { Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.observaciones), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
