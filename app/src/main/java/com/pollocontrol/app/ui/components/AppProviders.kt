package com.pollocontrol.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.pollocontrol.app.data.auth.AuthManager
import com.pollocontrol.app.data.settings.SettingsManager
import com.pollocontrol.app.data.sync.FirebaseSyncManager

data class AppDependencies(
    val settingsManager: SettingsManager,
    val firebaseSyncManager: FirebaseSyncManager,
    val authManager: AuthManager
)

val LocalAppDependencies = staticCompositionLocalOf<AppDependencies> {
    error("No AppDependencies provided")
}

@Composable
fun AppProviders(
    settingsManager: SettingsManager,
    firebaseSyncManager: FirebaseSyncManager,
    authManager: AuthManager,
    content: @Composable () -> Unit
) {
    val deps = AppDependencies(settingsManager, firebaseSyncManager, authManager)
    CompositionLocalProvider(LocalAppDependencies provides deps) {
        content()
    }
}
