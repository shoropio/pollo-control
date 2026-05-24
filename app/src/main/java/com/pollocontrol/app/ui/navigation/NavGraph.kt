package com.pollocontrol.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.ui.dashboard.DashboardScreen
import com.pollocontrol.app.ui.batches.BatchesScreen
import com.pollocontrol.app.ui.batches.BatchDetailScreen
import com.pollocontrol.app.ui.batches.BatchFormScreen
import com.pollocontrol.app.ui.inventory.InventoryScreen
import com.pollocontrol.app.ui.inventory.InventoryFormScreen
import com.pollocontrol.app.ui.feeding.FeedingScreen
import com.pollocontrol.app.ui.expenses.ExpensesScreen
import com.pollocontrol.app.ui.expenses.ExpenseFormScreen
import com.pollocontrol.app.ui.health.HealthScreen
import com.pollocontrol.app.ui.weighing.WeighingScreen
import com.pollocontrol.app.ui.slaughter.SlaughterScreen
import com.pollocontrol.app.ui.sales.SalesScreen
import com.pollocontrol.app.ui.sales.SaleFormScreen
import com.pollocontrol.app.ui.clients.ClientsScreen
import com.pollocontrol.app.ui.clients.ClientFormScreen
import com.pollocontrol.app.ui.reports.ReportsScreen
import com.pollocontrol.app.ui.settings.SettingsScreen
import com.pollocontrol.app.ui.quail.QuailBatchesScreen
import com.pollocontrol.app.ui.eggs.EggProductionScreen
import com.pollocontrol.app.ui.components.PolloBottomNavBar
import com.pollocontrol.app.ui.mortality.MortalityScreen
import com.pollocontrol.app.ui.navigation.MoreScreen
import com.pollocontrol.app.ui.backuprestore.BackupRestoreScreen
import kotlinx.coroutines.launch

@Composable
fun PolloControlNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val app = LocalContext.current.applicationContext as PolloControlApp
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                app = app,
                navController = navController,
                onNavigateToBatches = { navController.navigate(Screen.Batches.route) { popUpTo(Screen.Dashboard.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) { popUpTo(Screen.Dashboard.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                onNavigateToSales = { navController.navigate(Screen.Sales.route) { popUpTo(Screen.Dashboard.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                onNavigateToClients = { navController.navigate(Screen.Clients.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToBatchDetail = { batchId -> navController.navigate(Screen.BatchDetail.createRoute(batchId)) }
            )
        }

        composable(Screen.Batches.route) {
            BatchesScreen(
                app = app,
                navController = navController,
                onNavigateToDetail = { batchId -> navController.navigate(Screen.BatchDetail.createRoute(batchId)) },
                onNavigateToForm = { batchId -> navController.navigate(Screen.BatchForm.createRoute(batchId)) }
            )
        }

        composable(
            route = Screen.BatchDetail.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            BatchDetailScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMortality = { navController.navigate(Screen.Mortality.createRoute(loteId)) },
                onNavigateToFeeding = { navController.navigate(Screen.Feeding.createRoute(loteId)) },
                onNavigateToHealth = { navController.navigate(Screen.Health.createRoute(loteId)) },
                onNavigateToWeighing = { navController.navigate(Screen.Weighing.createRoute(loteId)) },
                onNavigateToSlaughter = { navController.navigate(Screen.Slaughter.createRoute(loteId)) },
                onNavigateToEdit = { navController.navigate(Screen.BatchForm.createRoute(loteId)) }
            )
        }

        composable(
            route = Screen.BatchForm.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            BatchFormScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Mortality.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            MortalityScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                app = app,
                navController = navController,
                onNavigateToForm = { insumoId -> navController.navigate(Screen.InventoryForm.createRoute(insumoId)) }
            )
        }

        composable(
            route = Screen.InventoryForm.route,
            arguments = listOf(navArgument("insumoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val insumoId = backStackEntry.arguments?.getLong("insumoId") ?: 0L
            InventoryFormScreen(
                insumoId = insumoId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Feeding.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            FeedingScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Expenses.route) {
            ExpensesScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { gastoId -> navController.navigate(Screen.ExpenseForm.createRoute(gastoId)) }
            )
        }

        composable(
            route = Screen.ExpenseForm.route,
            arguments = listOf(navArgument("gastoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gastoId = backStackEntry.arguments?.getLong("gastoId") ?: 0L
            ExpenseFormScreen(
                gastoId = gastoId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Health.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            HealthScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Weighing.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            WeighingScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Slaughter.route,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            SlaughterScreen(
                batchId = loteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Sales.route) {
            SalesScreen(
                app = app,
                navController = navController,
                onNavigateToForm = { ventaId -> navController.navigate(Screen.SaleForm.createRoute(ventaId)) }
            )
        }

        composable(Screen.More.route) {
            MoreScreen(
                navController = navController,
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                onNavigateToClients = { navController.navigate(Screen.Clients.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToQuailBatches = { navController.navigate(Screen.QuailBatches.route) },
                onNavigateToEggProduction = { navController.navigate(Screen.EggProduction.route) },
                onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onSync = { app.firebaseSyncManager.syncAll() },
                currentUser = app.authManager.currentUser.value,
                onSignOut = { scope.launch { app.authManager.signOut() } }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() },
                onSignOut = { scope.launch { app.authManager.signOut() } }
            )
        }

        composable(
            route = Screen.SaleForm.route,
            arguments = listOf(navArgument("ventaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getLong("ventaId") ?: 0L
            SaleFormScreen(
                ventaId = ventaId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Clients.route) {
            ClientsScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForm = { clienteId -> navController.navigate(Screen.ClientForm.createRoute(clienteId)) }
            )
        }

        composable(
            route = Screen.ClientForm.route,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: 0L
            ClientFormScreen(
                clienteId = clienteId,
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QuailBatches.route) {
            QuailBatchesScreen(
                app = app,
                navController = navController
            )
        }

        composable(Screen.EggProduction.route) {
            EggProductionScreen(
                app = app,
                navController = navController
            )
        }

        composable(Screen.BackupRestore.route) {
            BackupRestoreScreen(
                app = app,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
