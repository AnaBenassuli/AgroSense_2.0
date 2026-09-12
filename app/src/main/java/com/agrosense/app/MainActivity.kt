package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.agrosense.app.local.LocalAuthManager
import com.agrosense.app.navigation.MainNavHost
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.theme.AgroSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authManager = LocalAuthManager(this)
        // Si el usuario no marcó "Recordarme", la sesión no debe sobrevivir al
        // cierre de la app. Se limpia solo en un arranque real del proceso:
        // onCreate también corre en cambios de configuración y restauración
        // tras muerte de proceso, y en ambos casos rememberNavController()
        // restaura el back stack, así que la sesión debe seguir viva.
        if (savedInstanceState == null) {
            authManager.limpiarSesionNoRecordada()
        }
        val destinoInicial =
            if (authManager.isLoggedIn()) Route.Home.route else Route.Login.route

        setContent {
            AgroSenseTheme {
                Surface(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    val navController = rememberNavController()
                    MainNavHost(
                        navController = navController,
                        startDestination = destinoInicial
                    )
                }
            }
        }
    }
}
