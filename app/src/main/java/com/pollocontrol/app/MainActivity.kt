/*
 * Copyright © 2026. Shoropio Corporation
 * Todos los derechos reservados.
 */

package com.pollocontrol.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pollocontrol.app.data.auth.AuthManager
import com.pollocontrol.app.data.settings.SettingsManager
import com.pollocontrol.app.data.sync.FirebaseSyncManager
import com.pollocontrol.app.ui.auth.LoginScreen
import com.pollocontrol.app.ui.components.AppProviders
import com.pollocontrol.app.ui.navigation.PolloControlNavGraph
import com.pollocontrol.app.ui.theme.PolloControlTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var firebaseSyncManager: FirebaseSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolloControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppProviders(
                        settingsManager = settingsManager,
                        firebaseSyncManager = firebaseSyncManager,
                        authManager = authManager
                    ) {
                        val currentUser by authManager.currentUser.collectAsState()

                        if (currentUser == null) {
                            LoginScreen(
                                authManager = authManager,
                                onSignedIn = {}
                            )
                        } else {
                            PolloControlNavGraph()
                        }
                    }
                }
            }
        }
    }
}
