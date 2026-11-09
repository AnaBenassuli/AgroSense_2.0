package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.agrosense.app.ui.navigation.AppNavigation
import com.agrosense.app.ui.screen.SplashScreen
import com.agrosense.app.ui.theme.AgroSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AgroSenseTheme {

                // La animación solo se muestra al abrir la app (no al girar el teléfono)
                var mostrarSplash by rememberSaveable {
                    mutableStateOf(true)
                }

                if (mostrarSplash) {
                    SplashScreen(
                        onTerminado = { mostrarSplash = false }
                    )
                } else {
                    AppNavigation()
                }
            }
        }
    }
}