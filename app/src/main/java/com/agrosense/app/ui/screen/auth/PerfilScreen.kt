package com.agrosense.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroCardActions
import com.agrosense.app.ui.components.AgroDialog
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroSegmented
import com.agrosense.app.ui.components.AgroStatusChip
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.AgroTopBar
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroGreenSoft
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.viewmodel.UsuarioViewModel

@Composable
fun PerfilScreen(
    usuarioViewModel: UsuarioViewModel,
    onBackClick: () -> Unit
) {

    val usuarios by usuarioViewModel.usuarios.collectAsState()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var usuarioEditando by remember {
        mutableStateOf<Usuario?>(null)
    }

    var usuarioAEliminar by remember {
        mutableStateOf<Usuario?>(null)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var tipoUsuario by remember {
        mutableStateOf("Productor")
    }

    val tiposUsuario = listOf(
        "Productor",
        "Agrónomo",
        "Cooperativa"
    )

    fun limpiarFormulario() {

        nombre = ""
        correo = ""
        tipoUsuario = "Productor"

        usuarioEditando = null
    }

    fun nuevoUsuario() {

        limpiarFormulario()

        mostrarFormulario = true
    }

    fun editarUsuario(
        usuario: Usuario
    ) {

        usuarioEditando = usuario

        nombre = usuario.nombre
        correo = usuario.correo
        tipoUsuario = usuario.tipoUsuario

        mostrarFormulario = true
    }

    AgroScreen(
        bottomBar = {
            AgroPrimaryButton(
                text = "Agregar usuario",
                icon = R.drawable.ic_add,
                onClick = { nuevoUsuario() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        AgroTopBar(
            title = "Perfil y usuarios",
            onBack = onBackClick
        )

        Text(
            text = "Administra los usuarios registrados en AgroSense.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (usuarios.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_group,
                title = "No hay usuarios registrados",
                description = "Agrega un usuario para comenzar."
            )

        } else {

            AgroSectionHeader(
                title = "Usuarios",
                trailing =
                    if (usuarios.size == 1) {
                        "1 registrado"
                    } else {
                        "${usuarios.size} registrados"
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(
                    items = usuarios,
                    key = {
                        it.id
                    }
                ) { usuario ->

                    UsuarioCard(
                        usuario = usuario,
                        onEditClick = {
                            editarUsuario(usuario)
                        },
                        onDeleteClick = {
                            usuarioAEliminar =
                                usuario
                        }
                    )
                }
            }
        }
    }

    // ==================================================
    // CREATE / UPDATE
    // ==================================================

    if (mostrarFormulario) {

        val formularioCompleto =
            nombre.isNotBlank() &&
                    correo.isNotBlank()

        AgroDialog(
            title =
                if (usuarioEditando == null) {
                    "Nuevo usuario"
                } else {
                    "Editar usuario"
                },
            confirmText =
                if (usuarioEditando == null) {
                    "Guardar"
                } else {
                    "Actualizar"
                },
            confirmEnabled = formularioCompleto,
            onDismiss = {
                mostrarFormulario = false
                limpiarFormulario()
            },
            onConfirm = {

                if (formularioCompleto) {

                    val actual =
                        usuarioEditando

                    if (actual == null) {

                        // CREATE
                        usuarioViewModel
                            .registrarUsuario(
                                nombre =
                                    nombre.trim(),
                                correo =
                                    correo.trim(),
                                tipoUsuario =
                                    tipoUsuario
                            )

                    } else {

                        // UPDATE
                        usuarioViewModel
                            .actualizarUsuario(
                                actual.copy(
                                    nombre =
                                        nombre.trim(),
                                    correo =
                                        correo.trim(),
                                    tipoUsuario =
                                        tipoUsuario
                                )
                            )
                    }

                    mostrarFormulario =
                        false

                    limpiarFormulario()
                }
            }
        ) {

            AgroTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre",
                placeholder = "Nombre completo",
                leadingIcon = R.drawable.ic_person,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            AgroTextField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                placeholder = "correo@ejemplo.com",
                leadingIcon = R.drawable.ic_mail,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            Text(
                text = "Tipo de usuario",
                style = MaterialTheme.typography.labelMedium,
                color = AgroTextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            AgroSegmented(
                options = tiposUsuario.map { it to it },
                selected = tipoUsuario,
                onSelect = { tipoUsuario = it }
            )
        }
    }

    // ==================================================
    // DELETE
    // ==================================================

    usuarioAEliminar?.let { usuario ->

        AgroDialog(
            title = "Eliminar usuario",
            confirmText = "Eliminar",
            destructive = true,
            onDismiss = {
                usuarioAEliminar = null
            },
            onConfirm = {
                usuarioViewModel
                    .eliminarUsuario(
                        usuario
                    )

                usuarioAEliminar =
                    null
            }
        ) {

            Text(
                text = "¿Deseas eliminar a \"${usuario.nombre}\"?",
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodySmall,
                color = AgroTextSecondary
            )
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    AgroCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar con iniciales
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AgroGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(usuario.nombre),
                    style = MaterialTheme.typography.titleSmall,
                    color = AgroGreenDark
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = AgroTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AgroStatusChip(
                text = usuario.tipoUsuario,
                tone = AgroTone.ACCENT
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AgroCardActions(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

private fun iniciales(nombre: String): String =
    nombre
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }