package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.ui.components.BotonRegresar
import com.agrosense.app.ui.components.OpcionTipoUsuario
import com.agrosense.app.ui.screen.auth.UserType

/**
 * Datos de la cuenta con la sesión iniciada. Antes era "Perfil y usuarios": una lista de todas
 * las cuentas del teléfono que cualquiera podía editar o borrar.
 */
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onGuardar: (nombre: String, tipoUsuario: String) -> Unit,
    onCerrarSesion: () -> Unit,
    onBackClick: () -> Unit
) {

    // Se reinicia si cambia el usuario (por ejemplo, al guardar se recibe el actualizado)
    var nombre by remember(usuario) {
        mutableStateOf(usuario?.nombre.orEmpty())
    }

    var tipo by remember(usuario) {
        mutableStateOf(UserType.desdeEtiqueta(usuario?.tipoUsuario.orEmpty()))
    }

    var mensaje by remember(usuario) {
        mutableStateOf<String?>(null)
    }

    val hayCambios =
        usuario != null &&
            (nombre.trim() != usuario.nombre || tipo.label != usuario.tipoUsuario)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BotonRegresar(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Consulta y edita los datos de tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (usuario == null) {
            Text("No hay una sesión iniciada.")
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Es con lo que inicias sesión; no se puede cambiar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        mensaje = null
                    },
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "¿Cómo usas AgroSense?",
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

                UserType.entries.forEach { opcion ->

                    OpcionTipoUsuario(
                        tipo = opcion,
                        seleccionado = opcion == tipo,
                        onClick = {
                            tipo = opcion
                            mensaje = null
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                mensaje?.let {

                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            mensaje = "Escribe tu nombre."
                        } else {
                            onGuardar(nombre.trim(), tipo.label)
                        }
                    },
                    enabled = hayCambios,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Guardar cambios")
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}
