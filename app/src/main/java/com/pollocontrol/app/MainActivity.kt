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
import com.pollocontrol.app.ui.auth.LoginScreen
import com.pollocontrol.app.ui.navigation.PolloControlNavGraph
import com.pollocontrol.app.ui.theme.PolloControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolloControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val app = applicationContext as PolloControlApp
                    val currentUser by app.authManager.currentUser.collectAsState()

                    if (currentUser == null) {
                        LoginScreen(
                            authManager = app.authManager,
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
