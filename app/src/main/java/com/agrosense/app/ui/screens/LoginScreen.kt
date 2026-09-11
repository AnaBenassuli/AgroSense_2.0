package com.agrosense.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agrosense.app.local.LocalAuthManager
import com.agrosense.app.navigation.Route

/**
 * Acceso local: sólo nombre, sin correo ni contraseña (Sección 2 de la
 * ficha técnica, "acceso local sencillo para el prototipo").
 */
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember(context) { LocalAuthManager(context) }

    var nombre by remember { mutableStateOf("") }
    var recordarme by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    fun entrar() {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) {
            error = "Por favor ingresa tu nombre"
            return
        }
        authManager.iniciarSesion(limpio, recordarme)
        navController.navigate(Route.Home.route) {
            popUpTo(Route.Login.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AgroSense", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Diagnóstico de plagas y enfermedades",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                if (error.isNotEmpty()) error = ""
            },
            label = { Text("Tu nombre") },
            singleLine = true,
            isError = error.isNotEmpty(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { entrar() }),
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = recordarme, onCheckedChange = { recordarme = it })
            Text("Recordarme en este dispositivo")
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = { entrar() }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Tus datos se guardan sólo en este teléfono.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
