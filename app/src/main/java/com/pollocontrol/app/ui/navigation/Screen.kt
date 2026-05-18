package com.pollocontrol.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Batches : Screen("batches", "Lotes")
    data object BatchDetail : Screen("batches/{loteId}", "Detalle del Lote") {
        fun createRoute(loteId: Long) = "batches/$loteId"
    }
    data object BatchForm : Screen("batches/form/{loteId}", "Formulario Lote") {
        fun createRoute(loteId: Long = 0L) = if (loteId == 0L) "batches/form/0" else "batches/form/$loteId"
    }
    data object Mortality : Screen("mortality/{loteId}", "Mortalidad") {
        fun createRoute(loteId: Long) = "mortality/$loteId"
    }
    data object Inventory : Screen("inventory", "Inventario")
    data object InventoryForm : Screen("inventory/form/{insumoId}", "Formulario Insumo") {
        fun createRoute(insumoId: Long = 0L) = if (insumoId == 0L) "inventory/form/0" else "inventory/form/$insumoId"
    }
    data object Feeding : Screen("feeding/{loteId}", "Alimentación") {
        fun createRoute(loteId: Long) = "feeding/$loteId"
    }
    data object Expenses : Screen("expenses", "Gastos")
    data object ExpenseForm : Screen("expenses/form/{gastoId}", "Formulario Gasto") {
        fun createRoute(gastoId: Long = 0L) = if (gastoId == 0L) "expenses/form/0" else "expenses/form/$gastoId"
    }
    data object Health : Screen("health/{loteId}", "Sanidad") {
        fun createRoute(loteId: Long) = "health/$loteId"
    }
    data object Weighing : Screen("weighing/{loteId}", "Pesajes") {
        fun createRoute(loteId: Long) = "weighing/$loteId"
    }
    data object Slaughter : Screen("slaughter/{loteId}", "Sacrificio") {
        fun createRoute(loteId: Long) = "slaughter/$loteId"
    }
    data object Sales : Screen("sales", "Ventas")
    data object SaleForm : Screen("sales/form/{ventaId}", "Formulario Venta") {
        fun createRoute(ventaId: Long = 0L) = if (ventaId == 0L) "sales/form/0" else "sales/form/$ventaId"
    }
    data object Clients : Screen("clients", "Clientes")
    data object ClientForm : Screen("clients/form/{clienteId}", "Formulario Cliente") {
        fun createRoute(clienteId: Long = 0L) = if (clienteId == 0L) "clients/form/0" else "clients/form/$clienteId"
    }
    data object Reports : Screen("reports", "Reportes")
    data object More : Screen("more", "Más")
    data object Settings : Screen("settings", "Configuraciones")
    data object BackupRestore : Screen("backup_restore", "Copia de Seguridad")
}
