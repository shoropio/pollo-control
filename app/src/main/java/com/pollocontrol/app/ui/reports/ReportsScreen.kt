package com.pollocontrol.app.ui.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pollocontrol.app.data.settings.AppCurrency
import com.pollocontrol.app.domain.model.BatchReport
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.settings.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ReportsViewModel = hiltViewModel()
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()
    val context = LocalContext.current

    var exportMessage by remember { mutableStateOf<String?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                out.write(viewModel.getCsvContent().toByteArray())
                exportMessage = "CSV exportado exitosamente"
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                out.write(viewModel.getPdfContent())
                exportMessage = "PDF exportado exitosamente"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, navigationIconContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    onClick = { viewModel.generateAllReports() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Assessment, "Generar", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Generar", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(

                    shape = androidx.compose.ui.graphics.RectangleShape,
                    onClick = {
                        if (reports.isNotEmpty()) {
                            csvLauncher.launch("reportes_pollocontrol.csv")
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = reports.isNotEmpty() && !isLoading
                ) {
                    Icon(Icons.Default.TableChart, "CSV", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("CSV", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(

                    shape = androidx.compose.ui.graphics.RectangleShape,
                    onClick = {
                        if (reports.isNotEmpty()) {
                            pdfLauncher.launch("reportes_pollocontrol.pdf")
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = reports.isNotEmpty() && !isLoading
                ) {
                    Icon(Icons.Default.PictureAsPdf, "PDF", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PDF", fontWeight = FontWeight.Bold)
                }
            }

            exportMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                LaunchedEffect(msg) { kotlinx.coroutines.delay(3000); exportMessage = null }
            }

            if (reports.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Presione 'Generar' para ver los datos", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (reports.isNotEmpty()) {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reports) { report ->
                        ReportCard(report, currency)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: BatchReport, currency: AppCurrency) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(report.batchName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Raza", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(report.breed, style = MaterialTheme.typography.bodyMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Edad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${report.age} dias", style = MaterialTheme.typography.bodyMedium) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Iniciales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${report.initialCount}", style = MaterialTheme.typography.bodyMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Vivos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${report.aliveCount}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF388E3C)) }
                Column(horizontalAlignment = Alignment.End) { Text("Mortandad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${"%.1f".format(report.mortalityPercentage)}%", style = MaterialTheme.typography.bodyMedium, color = if (report.mortalityPercentage > 10) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Costo Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(report.totalCost, currency), style = MaterialTheme.typography.bodyMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Ventas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(report.totalSales, currency), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1976D2)) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Costo/Pollo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(report.costPerChicken, currency), style = MaterialTheme.typography.bodyMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Costo/Kilo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(report.costPerKilo, currency), style = MaterialTheme.typography.bodyMedium) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Conversion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${"%.2f".format(report.feedConversion)}", style = MaterialTheme.typography.bodyMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Peso Prom.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${"%.3f".format(report.avgWeight)} kg", style = MaterialTheme.typography.bodyMedium) }
            }

            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Utilidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatMoney(report.profit, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (report.profit >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)) }
                Column(horizontalAlignment = Alignment.End) { Text("Rentabilidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${"%.1f".format(report.profitability)}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (report.profitability >= 0) Color(0xFF388E3C) else Color(0xFFD32F2F)) }
            }
        }
    }
}
