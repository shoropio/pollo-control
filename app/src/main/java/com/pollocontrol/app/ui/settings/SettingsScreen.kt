@file:OptIn(ExperimentalMaterial3Api::class)

package com.pollocontrol.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pollocontrol.app.PolloControlApp
import com.pollocontrol.app.data.settings.AppCurrency
import com.pollocontrol.app.data.sync.SyncStatus
import com.pollocontrol.app.ui.theme.BluePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    app: PolloControlApp,
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val currency by app.settingsManager.currency.collectAsState()
    val currentUser by app.authManager.currentUser.collectAsState()
    val syncStatus by app.firebaseSyncManager.syncStatus.collectAsState()
    val syncResult by app.firebaseSyncManager.lastSyncResult.collectAsState()
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuraciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSectionTitle("Preferencias")
            CurrencySettingCard(currency, app.settingsManager::setCurrency)

            SettingsSectionTitle("Cuenta")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp).clip(RectangleShape),
                        color = BluePrimary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentUser?.displayName ?: "Sin sesion", fontWeight = FontWeight.Medium)
                        Text(
                            currentUser?.email ?: "No hay usuario activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onSignOut) {
                        Text("Salir")
                    }
                }
            }

            SettingsSectionTitle("Firebase")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (syncStatus == SyncStatus.NOT_CONFIGURED) Icons.Default.CloudOff else Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Sincronizacion", fontWeight = FontWeight.Medium)
                            Text(
                                syncStatusLabel(syncStatus),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    syncResult?.message?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (syncResult?.success == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    ElevatedButton(
                        onClick = {
                            isSyncing = true
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    app.firebaseSyncManager.syncAll()
                                }
                                isSyncing = false
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isSyncing) "Sincronizando" else "Probar sincronizacion")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun CurrencySettingCard(
    selectedCurrency: AppCurrency,
    onCurrencySelected: (AppCurrency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp).clip(RectangleShape),
                color = BluePrimary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = BluePrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Moneda", fontWeight = FontWeight.Medium)
                Text(
                    selectedCurrency.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ExpandMore, "Cambiar moneda")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    AppCurrency.entries.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency.label) },
                            leadingIcon = {
                                if (currency == selectedCurrency) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            },
                            onClick = {
                                onCurrencySelected(currency)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun syncStatusLabel(status: SyncStatus): String = when (status) {
    SyncStatus.PENDING -> "Pendiente"
    SyncStatus.SYNCING -> "Sincronizando"
    SyncStatus.SYNCED -> "Sincronizado"
    SyncStatus.ERROR -> "Error de sincronizacion"
    SyncStatus.NOT_CONFIGURED -> "Firebase no configurado"
}
