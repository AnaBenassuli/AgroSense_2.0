package com.agrosense.app.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.AgroSenseLogo

enum class UserType(
    val label: String
) {
    PRODUCER("Productor"),
    AGRONOMIST("Agrónomo"),
    COOPERATIVE("Cooperativa")
}

data class UserAccount(
    val nombre: String,
    val correo: String,
    val tipoUsuario: UserType
)

private enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthGate(
    onAuthenticated: (UserAccount) -> Unit
) {

    var mode by remember {
        mutableStateOf(AuthMode.LOGIN)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var tipoUsuario by remember {
        mutableStateOf(UserType.PRODUCER)
    }

    var tipoMenuAbierto by remember {
        mutableStateOf(false)
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 24.dp,
                vertical = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(34.dp)
        )

        AgroSenseLogo(
            compact = false
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "AgroSense",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Tecnología para cultivar con más información",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(34.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text =
                        if (mode == AuthMode.LOGIN) {
                            "Bienvenido"
                        } else {
                            "Crea tu cuenta"
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        if (mode == AuthMode.LOGIN) {
                            "Ingresa para consultar tus parcelas y diagnósticos."
                        } else {
                            "Regístrate para comenzar a administrar tus cultivos."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                if (mode == AuthMode.REGISTER) {

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            mensajeError = null
                        },
                        label = {
                            Text("Nombre")
                        },
                        placeholder = {
                            Text("Tu nombre")
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        mensajeError = null
                    },
                    label = {
                        Text("Correo electrónico")
                    },
                    placeholder = {
                        Text("correo@ejemplo.com")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensajeError = null
                    },
                    label = {
                        Text("Contraseña")
                    },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                if (mode == AuthMode.REGISTER) {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            mensajeError = null
                        },
                        label = {
                            Text("Confirmar contraseña")
                        },
                        singleLine = true,
                        visualTransformation =
                            PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "Tipo de usuario",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            tipoMenuAbierto = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Text(
                            text = tipoUsuario.label
                        )
                    }

                    DropdownMenu(
                        expanded = tipoMenuAbierto,
                        onDismissRequest = {
                            tipoMenuAbierto = false
                        }
                    ) {

                        UserType.entries.forEach { tipo ->

                            DropdownMenuItem(
                                text = {
                                    Text(tipo.label)
                                },
                                onClick = {
                                    tipoUsuario = tipo
                                    tipoMenuAbierto = false
                                }
                            )
                        }
                    }
                }

                mensajeError?.let { error ->

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {

                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                Button(
                    onClick = {

                        if (mode == AuthMode.LOGIN) {

                            if (
                                correo.isBlank() ||
                                password.isBlank()
                            ) {

                                mensajeError =
                                    "Ingresa tu correo y contraseña."

                            } else {

                                onAuthenticated(
                                    UserAccount(
                                        nombre = "Usuario",
                                        correo = correo.trim(),
                                        tipoUsuario =
                                            UserType.PRODUCER
                                    )
                                )
                            }

                        } else {

                            when {

                                nombre.isBlank() -> {
                                    mensajeError =
                                        "Ingresa tu nombre."
                                }

                                correo.isBlank() -> {
                                    mensajeError =
                                        "Ingresa tu correo electrónico."
                                }

                                password.length < 4 -> {
                                    mensajeError =
                                        "La contraseña debe tener al menos 4 caracteres."
                                }

                                password != confirmPassword -> {
                                    mensajeError =
                                        "Las contraseñas no coinciden."
                                }

                                else -> {

                                    onAuthenticated(
                                        UserAccount(
                                            nombre = nombre.trim(),
                                            correo = correo.trim(),
                                            tipoUsuario =
                                                tipoUsuario
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            if (mode == AuthMode.LOGIN) {
                                "Iniciar sesión"
                            } else {
                                "Crear cuenta"
                            },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            if (mode == AuthMode.LOGIN) {
                                "¿Aún no tienes cuenta?"
                            } else {
                                "¿Ya tienes cuenta?"
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = {

                            mode =
                                if (
                                    mode == AuthMode.LOGIN
                                ) {
                                    AuthMode.REGISTER
                                } else {
                                    AuthMode.LOGIN
                                }

                            mensajeError = null
                            password = ""
                            confirmPassword = ""
                        }
                    ) {

                        Text(
                            text =
                                if (mode == AuthMode.LOGIN) {
                                    "Regístrate"
                                } else {
                                    "Inicia sesión"
                                },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Cultiva · Analiza · Decide",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )
    }
}