package com.agrosense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
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
        // cierre de la app. Se limpia antes de decidir la pantalla inicial.
        authManager.limpiarSesionNoRecordada()
        val destinoInicial =
            if (authManager.isLoggedIn()) Route.Home.route else Route.Login.route

        setContent {
            AgroSenseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    MainNavHost(
                        navController = navController,
                        startDestination = remember { destinoInicial }
                    )
                }
            }
        }
    }
}
