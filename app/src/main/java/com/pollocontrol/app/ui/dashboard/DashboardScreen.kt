package com.pollocontrol.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pollocontrol.app.ui.components.KpiCard
import com.pollocontrol.app.ui.components.LocalAppDependencies
import com.pollocontrol.app.ui.components.MiniLineChart
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.components.PullToSyncBox
import com.pollocontrol.app.ui.settings.formatMoney
import com.pollocontrol.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.pollocontrol.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun DashboardScreen(
    navController: NavHostController,
    onNavigateToBatches: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBatchDetail: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val settingsManager = LocalAppDependencies.current.settingsManager
    val currency by settingsManager.currency.collectAsState()
    val firebaseSyncManager = LocalAppDependencies.current.firebaseSyncManager

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    Text(
                        text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        PullToSyncBox(
            onSync = {
                firebaseSyncManager.syncAll()
                viewModel.loadDashboard(forceRefresh = true)
            },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            Text(
                text = stringResource(R.string.buenos_dias),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.resumen_general_granja),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isLoading) {
                // Shimmer-style loading
                repeat(4) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {}
                }
            } else {
                // KPI Grid - compact 3 columns
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(stringResource(R.string.lotes), stats.activeLotes.toString(), Icons.Default.Agriculture, BluePrimary, Modifier.weight(1f))
                    KpiCard(stringResource(R.string.vivos), stats.totalLiveChickens.toString(), Icons.Default.Face, StatusPositive, Modifier.weight(1f))
                    KpiCard(stringResource(R.string.mort), stats.totalMortality.toString(), Icons.Default.Warning, StatusNegative, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard(stringResource(R.string.ventas), formatMoney(stats.totalSales, currency, decimals = 0), Icons.Default.ShoppingCart, StatusInfo, Modifier.weight(1f))
                    KpiCard(stringResource(R.string.gastos), formatMoney(stats.totalExpenses, currency, decimals = 0), Icons.Default.AttachMoney, StatusWarning, Modifier.weight(1f))
                    KpiCard(stringResource(R.string.ganancia), formatMoney(stats.estimatedProfit, currency, decimals = 0), Icons.Default.AccountBalance, if (stats.estimatedProfit >= 0) StatusPositive else StatusNegative, Modifier.weight(1f))
                }

                // Chart
                MiniLineChart(
                    dataPoints = listOf(
                        stats.salesToday.toFloat(),
                        stats.salesWeek.toFloat() / 2f,
                        stats.salesMonth.toFloat() / 4f
                    ).ifEmpty { listOf(0f, 0f, 0f) },
                    lineColor = BluePrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Actions
                Text(
                    stringResource(R.string.acceso_rapido),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard(stringResource(R.string.lotes), Icons.Default.Agriculture, BluePrimary, onNavigateToBatches, Modifier.weight(1f))
                    QuickActionCard(stringResource(R.string.inventario), Icons.Default.Inventory2, StatusPositive, onNavigateToInventory, Modifier.weight(1f))
                    QuickActionCard(stringResource(R.string.ventas), Icons.Default.ShoppingCart, StatusInfo, onNavigateToSales, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard(stringResource(R.string.gastos), Icons.Default.AttachMoney, StatusWarning, onNavigateToExpenses, Modifier.weight(1f))
                    QuickActionCard(stringResource(R.string.clientes), Icons.Default.People, AccentRose, onNavigateToClients, Modifier.weight(1f))
                    QuickActionCard(stringResource(R.string.reportes), Icons.Default.Assessment, AccentGreen, onNavigateToReports, Modifier.weight(1f))
                }

                // Alerts
                if (stats.lowStockItems > 0 || stats.mortalityRate > 10 || stats.pendingPayments > 0) {
                    Text(
                        stringResource(R.string.alertas_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (stats.lowStockItems > 0)
                        AlertCard(stringResource(R.string.insumos_stock_bajo, stats.lowStockItems), StatusWarning, Icons.Default.Inventory2)
                    if (stats.mortalityRate > 10)
                        AlertCard(stringResource(R.string.mortalidad_formato, String.format("%.1f", stats.mortalityRate)), StatusNegative, Icons.Default.Warning)
                    if (stats.pendingPayments > 0)
                        AlertCard(stringResource(R.string.pagos_pendientes, stats.pendingPayments), StatusInfo, Icons.Default.Payment)
                }
            }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, title, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AlertCard(message: String, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}
