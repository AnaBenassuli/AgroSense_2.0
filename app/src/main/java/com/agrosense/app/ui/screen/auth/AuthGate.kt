package com.agrosense.app.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.ui.components.AgroBanner
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroSegmented
import com.agrosense.app.ui.components.AgroSenseLogo
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroBackground
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
import com.agrosense.app.ui.viewmodel.EstadoAcceso

enum class UserType(
    val label: String
) {
    PRODUCER("Productor"),
    AGRONOMIST("Agrónomo"),
    COOPERATIVE("Cooperativa")
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

    fun cambiarModo() {
        mode =
            if (mode == AuthMode.LOGIN) {
                AuthMode.REGISTER
            } else {
                AuthMode.LOGIN
            }

        limpiarErrores()
        password = ""
        confirmPassword = ""
    }

    fun enviar() {

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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // MARCA
        AgroSenseLogo(
            modifier = Modifier.fillMaxWidth(0.62f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Tecnología para cultivar con más información",
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // ENCABEZADO DEL FORMULARIO
        Column(modifier = Modifier.fillMaxWidth()) {

            Text(
                text =
                    if (mode == AuthMode.LOGIN) {
                        "Bienvenido"
                    } else {
                        "Crea tu cuenta"
                    },
                style = MaterialTheme.typography.headlineMedium,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    if (mode == AuthMode.LOGIN) {
                        "Ingresa para consultar tus parcelas y diagnósticos."
                    } else {
                        "Regístrate para comenzar a administrar tus cultivos."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CAMPOS
        if (mode == AuthMode.REGISTER) {

            AgroTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    limpiarErrores()
                },
                label = "Nombre",
                placeholder = "Tu nombre",
                leadingIcon = R.drawable.ic_person,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()
        }

        AgroTextField(
            value = correo,
            onValueChange = {
                correo = it
                limpiarErrores()
            },
            label = "Correo electrónico",
            placeholder = "correo@ejemplo.com",
            leadingIcon = R.drawable.ic_mail,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        FieldSpacer()

        AgroTextField(
            value = password,
            onValueChange = {
                password = it
                limpiarErrores()
            },
            label = "Contraseña",
            placeholder =
                if (mode == AuthMode.REGISTER) {
                    "Mínimo $LONGITUD_MINIMA_CONTRASENA caracteres"
                } else {
                    "Tu contraseña"
                },
            leadingIcon = R.drawable.ic_lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (mode == AuthMode.REGISTER) {

            FieldSpacer()

            AgroTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    limpiarErrores()
                },
                label = "Confirmar contraseña",
                placeholder = "Repite tu contraseña",
                leadingIcon = R.drawable.ic_lock,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            Text(
                text = "Tipo de usuario",
                style = MaterialTheme.typography.labelMedium,
                color = AgroTextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            AgroSegmented(
                options = UserType.entries.map { it to it.label },
                selected = tipoUsuario,
                onSelect = { tipoUsuario = it }
            )
        }

        errorVisible?.let { error ->

            Spacer(modifier = Modifier.height(14.dp))

            AgroBanner(
                text = error,
                tone = AgroTone.ERROR
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        AgroPrimaryButton(
            text =
                when {
                    procesando -> "Verificando..."
                    mode == AuthMode.LOGIN -> "Iniciar sesión"
                    else -> "Crear cuenta"
                },
            icon = R.drawable.ic_arrow_forward,
            onClick = { enviar() },
            enabled = !procesando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // CAMBIO ENTRE INICIO DE SESIÓN Y REGISTRO
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
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextSecondary
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text =
                    if (mode == AuthMode.LOGIN) {
                        "Regístrate"
                    } else {
                        "Inicia sesión"
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgroGreenDark,
                modifier = Modifier
                    .clickable { cambiarModo() }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Cultiva · Analiza · Decide",
            style = MaterialTheme.typography.labelMedium,
            color = AgroTextTertiary
        )
    }
}