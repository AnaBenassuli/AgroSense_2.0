package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.AvisoHost
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

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (mostrarSplash) {
                        SplashScreen(
                            onTerminado = { mostrarSplash = false }
                        )
                    } else {
                        AppNavigation()
                    }

                    // Avisos de confirmación ("Parcela guardada"...), encima de todas las pantallas
                    AvisoHost(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}