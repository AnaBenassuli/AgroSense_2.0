package com.agrosense.app.ui.screen.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.agrosense.app.ui.viewmodel.EstadoAcceso

enum class UserType(
    val label: String,
    val descripcion: String
) {
    PRODUCER(
        "Productor",
        "Siembras o crías animales en tu propio terreno y quieres revisar tus cultivos o tu ganado."
    ),
    AGRONOMIST(
        "Agrónomo",
        "Asesoras a productores: revisas sus parcelas y das recomendaciones de manejo."
    ),
    COOPERATIVE(
        "Cooperativa",
        "Representas a un grupo de productores y llevas el registro de las parcelas de sus socios."
    )
}

private enum class AuthMode {
    LOGIN,
    REGISTER
}

private const val LONGITUD_MINIMA_CONTRASENA = 8

@Composable
fun AuthGate(
    estado: EstadoAcceso,
    onIniciarSesion: (correo: String, contrasena: String) -> Unit,
    onRegistrar: (nombre: String, correo: String, contrasena: String, tipoUsuario: String) -> Unit,
    onLimpiarError: () -> Unit
) {

    val procesando = estado is EstadoAcceso.Procesando

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

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    // Errores de validación locales primero; si no hay, el de la base (credenciales, correo en uso)
    val errorVisible =
        mensajeError ?: (estado as? EstadoAcceso.Error)?.mensaje

    fun limpiarErrores() {
        mensajeError = null
        onLimpiarError()
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
                            limpiarErrores()
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
                        limpiarErrores()
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
                        limpiarErrores()
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
                            limpiarErrores()
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
                        text = "¿Cómo vas a usar AgroSense?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Todas las cuentas tienen las mismas funciones; " +
                            "esto solo sirve para identificar tu perfil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    // Opciones a la vista en vez de un menú desplegable: así se lee la
                    // explicación de cada una antes de elegir
                    UserType.entries.forEach { tipo ->

                        OpcionTipoUsuario(
                            tipo = tipo,
                            seleccionado = tipo == tipoUsuario,
                            onClick = {
                                tipoUsuario = tipo
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }

                errorVisible?.let { error ->

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

                                onIniciarSesion(
                                    correo.trim(),
                                    password
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

                                password.length < LONGITUD_MINIMA_CONTRASENA -> {
                                    mensajeError =
                                        "La contraseña debe tener al menos $LONGITUD_MINIMA_CONTRASENA caracteres."
                                }

                                password != confirmPassword -> {
                                    mensajeError =
                                        "Las contraseñas no coinciden."
                                }

                                else -> {

                                    onRegistrar(
                                        nombre.trim(),
                                        correo.trim(),
                                        password,
                                        tipoUsuario.label
                                    )
                                }
                            }
                        }
                    },
                    enabled = !procesando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text =
                            when {
                                procesando -> "Verificando..."
                                mode == AuthMode.LOGIN -> "Iniciar sesión"
                                else -> "Crear cuenta"
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

                            limpiarErrores()
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

@Composable
private fun OpcionTipoUsuario(
    tipo: UserType,
    seleccionado: Boolean,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (seleccionado) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
        ),
        border =
            if (seleccionado) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            }
    ) {

        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = seleccionado,
                onClick = onClick
            )

            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {

                Text(
                    text = tipo.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = tipo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}