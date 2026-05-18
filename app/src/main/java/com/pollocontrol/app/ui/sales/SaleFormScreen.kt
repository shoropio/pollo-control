@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.local.entity.SaleEntity
import com.pollocontrol.app.ui.components.PolloDatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun SaleFormScreen(
    ventaId: Long,
    app: PolloControlApp,
    onNavigateBack: () -> Unit
) {
    val viewModel: SalesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SalesViewModel(app) as T
        }
    )
    val isEditing = ventaId != 0L
    val batches by viewModel.batches.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val currency by app.settingsManager.currency.collectAsState()

    var tipo by remember { mutableStateOf("SACRIFICADO") }
    var modalidad by remember { mutableStateOf("UNIDAD") }
    var cantidad by remember { mutableStateOf("") }
    var precioUnitario by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(System.currentTimeMillis()) }
    var batchId by remember { mutableStateOf<Long?>(null) }
    var clienteId by remember { mutableStateOf<Long?>(null) }
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var estadoPago by remember { mutableStateOf("PAGADO") }
    var observaciones by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var cantidadError by remember { mutableStateOf(false) }
    var precioError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    var tipoExpanded by remember { mutableStateOf(false) }
    var modalidadExpanded by remember { mutableStateOf(false) }
    var batchExpanded by remember { mutableStateOf(false) }
    var clienteExpanded by remember { mutableStateOf(false) }
    var pagoExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(ventaId) {
        if (isEditing) {
            viewModel.getById(ventaId) { v ->
                if (v != null) {
                    tipo = v.tipo; modalidad = v.modalidad; cantidad = v.cantidad.toString()
                    precioUnitario = v.precioUnitario.toString(); total = v.total.toString()
                    fecha = v.fecha; batchId = v.loteId; clienteId = v.clienteId
                    metodoPago = v.metodoPago; estadoPago = v.estadoPago; observaciones = v.observaciones
                }
                isLoading = false
            }
        } else isLoading = false
    }

    // Auto-calculate total
    val calculatedTotal = remember(cantidad, precioUnitario) {
        val cant = cantidad.toDoubleOrNull() ?: 0.0
        val prec = precioUnitario.toDoubleOrNull() ?: 0.0
        cant * prec
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Venta" else "Nueva Venta") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Tipo
                ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = !tipoExpanded }) {
                    OutlinedTextField(value = tipo, onValueChange = {}, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                        listOf("VIVO", "SACRIFICADO").forEach { t -> DropdownMenuItem(text = { Text(if (t == "VIVO") "Pollo Vivo" else "Pollo Sacrificado") }, onClick = { tipo = t; tipoExpanded = false }) }
                    }
                }

                // Modalidad
                ExposedDropdownMenuBox(expanded = modalidadExpanded, onExpandedChange = { modalidadExpanded = !modalidadExpanded }) {
                    OutlinedTextField(value = modalidad, onValueChange = {}, readOnly = true, label = { Text("Modalidad") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(modalidadExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = modalidadExpanded, onDismissRequest = { modalidadExpanded = false }) {
                        listOf("UNIDAD", "KILO").forEach { m -> DropdownMenuItem(text = { Text(if (m == "UNIDAD") "Por Unidad" else "Por Kilo") }, onClick = { modalidad = m; modalidadExpanded = false }) }
                    }
                }

                OutlinedTextField(value = cantidad, onValueChange = { cantidad = it; cantidadError = false; total = "" }, label = { Text("Cantidad *") }, isError = cantidadError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = precioUnitario, onValueChange = { precioUnitario = it; precioError = false; total = "" }, label = { Text("Precio Unitario *") }, isError = precioError, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })

                OutlinedTextField(value = if (total.isNotEmpty()) total else String.format("%.2f", calculatedTotal), onValueChange = { total = it }, label = { Text("Total") }, modifier = Modifier.fillMaxWidth(), singleLine = true, prefix = { Text(currency.code) })

                // Fecha
                val dateStr = remember(fecha) { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)) }
                OutlinedTextField(value = dateStr, onValueChange = {}, label = { Text("Fecha") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Fecha") } })

                // Lote selector
                var batchLabel by remember { mutableStateOf("Seleccionar lote (opcional)") }
                ExposedDropdownMenuBox(expanded = batchExpanded, onExpandedChange = { batchExpanded = !batchExpanded }) {
                    OutlinedTextField(value = batchLabel, onValueChange = {}, readOnly = true, label = { Text("Lote") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(batchExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = batchExpanded, onDismissRequest = { batchExpanded = false }) {
                        DropdownMenuItem(text = { Text("Sin lote") }, onClick = { batchId = null; batchLabel = "Sin lote"; batchExpanded = false })
                        batches.forEach { l -> DropdownMenuItem(text = { Text(l.nombre) }, onClick = { batchId = l.id; batchLabel = l.nombre; batchExpanded = false }) }
                    }
                }

                // Cliente selector
                var clienteLabel by remember { mutableStateOf("Seleccionar cliente (opcional)") }
                ExposedDropdownMenuBox(expanded = clienteExpanded, onExpandedChange = { clienteExpanded = !clienteExpanded }) {
                    OutlinedTextField(value = clienteLabel, onValueChange = {}, readOnly = true, label = { Text("Cliente") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clienteExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = clienteExpanded, onDismissRequest = { clienteExpanded = false }) {
                        DropdownMenuItem(text = { Text("Sin cliente") }, onClick = { clienteId = null; clienteLabel = "Sin cliente"; clienteExpanded = false })
                        clients.forEach { c -> DropdownMenuItem(text = { Text(c.nombre) }, onClick = { clienteId = c.id; clienteLabel = c.nombre; clienteExpanded = false }) }
                    }
                }

                // Método de pago
                ExposedDropdownMenuBox(expanded = pagoExpanded, onExpandedChange = { pagoExpanded = !pagoExpanded }) {
                    OutlinedTextField(value = metodoPago, onValueChange = {}, readOnly = true, label = { Text("Método de Pago") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pagoExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = pagoExpanded, onDismissRequest = { pagoExpanded = false }) {
                        listOf("EFECTIVO", "TRANSFERENCIA", "TARJETA", "CREDITO").forEach { p -> DropdownMenuItem(text = { Text(p.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { metodoPago = p; pagoExpanded = false }) }
                    }
                }

                // Estado de pago
                ExposedDropdownMenuBox(expanded = estadoExpanded, onExpandedChange = { estadoExpanded = !estadoExpanded }) {
                    OutlinedTextField(value = estadoPago, onValueChange = {}, readOnly = true, label = { Text("Estado de Pago") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(estadoExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = estadoExpanded, onDismissRequest = { estadoExpanded = false }) {
                        listOf("PAGADO", "PENDIENTE", "CREDITO").forEach { e -> DropdownMenuItem(text = { Text(e.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { estadoPago = e; estadoExpanded = false }) }
                    }
                }

                OutlinedTextField(value = observaciones, onValueChange = { observaciones = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                Spacer(Modifier.height(8.dp))
                Button(onClick = click@{
                    var valid = true
                    if (cantidad.isBlank() || cantidad.toDoubleOrNull() == null || cantidad.toDouble() <= 0) { cantidadError = true; valid = false }
                    if (precioUnitario.isBlank() || precioUnitario.toDoubleOrNull() == null || precioUnitario.toDouble() <= 0) { precioError = true; valid = false }
                    if (!valid) return@click
                    val finalTotal = if (total.isNotEmpty()) total.toDoubleOrNull() ?: calculatedTotal else calculatedTotal
                    viewModel.save(SaleEntity(
                        id = if (isEditing) ventaId else 0L,
                        loteId = batchId, clienteId = clienteId, fecha = fecha,
                        tipo = tipo, modalidad = modalidad, cantidad = cantidad.toDouble(),
                        precioUnitario = precioUnitario.toDouble(), total = finalTotal,
                        metodoPago = metodoPago, estadoPago = estadoPago, observaciones = observaciones
                    )) { onNavigateBack() }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (showDatePicker) { PolloDatePickerDialog(onDateSelected = { fecha = it }, onDismiss = { showDatePicker = false }) }
}
