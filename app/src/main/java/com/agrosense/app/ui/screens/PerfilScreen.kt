package com.agrosense.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agrosense.app.BuildConfig
import com.agrosense.app.local.LocalAuthManager
import com.agrosense.app.navigation.Route

/** Perfil del usuario local y cierre de sesión. */
@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember(context) { LocalAuthManager(context) }
    val userName = authManager.getUserName() ?: "Usuario"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Usuario", style = MaterialTheme.typography.labelMedium)
                Text(userName, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(12.dp))

                Text("Versión", style = MaterialTheme.typography.labelMedium)
                Text(
                    BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(12.dp))

                Text("Almacenamiento", style = MaterialTheme.typography.labelMedium)
                Text(
                    "Los datos se guardan sólo en este dispositivo (Room / SQLite). " +
                        "No hay sincronización con la nube.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                authManager.logout()
                navController.navigate(Route.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
