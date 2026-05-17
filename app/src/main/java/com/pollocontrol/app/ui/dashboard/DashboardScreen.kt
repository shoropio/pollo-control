package com.pollocontrol.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.ui.components.KpiCard
import com.pollocontrol.app.ui.components.MiniLineChart
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun DashboardScreen(
    app: PolloControlApp,
    navController: NavHostController,
    onNavigateToBatches: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBatchDetail: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(app) as T
        }
    )
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        bottomBar = { PolloBottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("PolloControl") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Buenos dias",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Resumen general de tu granja",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isLoading) {
                // Shimmer-style loading
                repeat(4) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {}
                }
            } else {
                // KPI Grid - compact 3 columns
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard("Lotes", stats.activeLotes.toString(), Icons.Default.Agriculture, BluePrimary, Modifier.weight(1f))
                    KpiCard("Vivos", stats.totalLiveChickens.toString(), Icons.Default.Face, StatusPositive, Modifier.weight(1f))
                    KpiCard("Mort.", stats.totalMortality.toString(), Icons.Default.Warning, StatusNegative, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard("Ventas", "$${String.format("%.0f", stats.totalSales)}", Icons.Default.ShoppingCart, StatusInfo, Modifier.weight(1f))
                    KpiCard("Gastos", "$${String.format("%.0f", stats.totalExpenses)}", Icons.Default.AttachMoney, StatusWarning, Modifier.weight(1f))
                    KpiCard("Ganancia", "$${String.format("%.0f", stats.estimatedProfit)}", Icons.Default.AccountBalance, if (stats.estimatedProfit >= 0) StatusPositive else StatusNegative, Modifier.weight(1f))
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
                    "Acceso Rapido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard("Lotes", Icons.Default.Agriculture, BluePrimary, onNavigateToBatches, Modifier.weight(1f))
                    QuickActionCard("Inventario", Icons.Default.Inventory2, StatusPositive, onNavigateToInventory, Modifier.weight(1f))
                    QuickActionCard("Ventas", Icons.Default.ShoppingCart, StatusInfo, onNavigateToSales, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionCard("Gastos", Icons.Default.AttachMoney, StatusWarning, onNavigateToExpenses, Modifier.weight(1f))
                    QuickActionCard("Clientes", Icons.Default.People, AccentRose, onNavigateToClients, Modifier.weight(1f))
                    QuickActionCard("Reportes", Icons.Default.Assessment, AccentGreen, onNavigateToReports, Modifier.weight(1f))
                }

                // Alerts
                if (stats.lowStockItems > 0 || stats.mortalityRate > 10 || stats.pendingPayments > 0) {
                    Text(
                        "Alertas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (stats.lowStockItems > 0)
                        AlertCard("${stats.lowStockItems} insumos con stock bajo", StatusWarning, Icons.Default.Inventory2)
                    if (stats.mortalityRate > 10)
                        AlertCard("Mortalidad: ${String.format("%.1f", stats.mortalityRate)}%", StatusNegative, Icons.Default.Warning)
                    if (stats.pendingPayments > 0)
                        AlertCard("${stats.pendingPayments} pagos pendientes", StatusInfo, Icons.Default.Payment)
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
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(8.dp),
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
