package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Usuario
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

    var mostrarTipos by remember {
        mutableStateOf(false)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Text(
                text = "Perfil y usuarios",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "Administra los usuarios registrados en AgroSense.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {
                nuevoUsuario()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape =
                RoundedCornerShape(14.dp)
        ) {

            Text("+ Agregar usuario")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (usuarios.isEmpty()) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(20.dp)
                ) {

                    Text(
                        text =
                            "No hay usuarios registrados",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "Agrega un usuario para comenzar.",
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

        } else {

            Text(
                text =
                    if (usuarios.size == 1) {
                        "1 usuario registrado"
                    } else {
                        "${usuarios.size} usuarios registrados"
                    },
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
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

        AlertDialog(
            onDismissRequest = {

                mostrarFormulario = false

                limpiarFormulario()
            },

            shape =
                RoundedCornerShape(18.dp),

            title = {

                Text(
                    text =
                        if (
                            usuarioEditando == null
                        ) {
                            "Nuevo usuario"
                        } else {
                            "Editar usuario"
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                        },
                        label = {
                            Text("Nombre")
                        },
                        singleLine = true,
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                        },
                        label = {
                            Text(
                                "Correo electrónico"
                            )
                        },
                        singleLine = true,
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Tipo de usuario",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Column {

                        OutlinedButton(
                            onClick = {
                                mostrarTipos = true
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Text(tipoUsuario)
                        }

                        DropdownMenu(
                            expanded =
                                mostrarTipos,
                            onDismissRequest = {
                                mostrarTipos =
                                    false
                            }
                        ) {

                            tiposUsuario.forEach {
                                    tipo ->

                                DropdownMenuItem(
                                    text = {
                                        Text(tipo)
                                    },
                                    onClick = {

                                        tipoUsuario =
                                            tipo

                                        mostrarTipos =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        if (
                            nombre.isNotBlank() &&
                            correo.isNotBlank()
                        ) {

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

                    Text(
                        if (
                            usuarioEditando == null
                        ) {
                            "Guardar"
                        } else {
                            "Actualizar"
                        }
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        mostrarFormulario =
                            false

                        limpiarFormulario()
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    // ==================================================
    // DELETE
    // ==================================================

    usuarioAEliminar?.let { usuario ->

        AlertDialog(
            onDismissRequest = {
                usuarioAEliminar = null
            },

            shape =
                RoundedCornerShape(18.dp),

            title = {

                Text(
                    text =
                        "Eliminar usuario",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "¿Deseas eliminar a \"${usuario.nombre}\"?"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Esta acción no se puede deshacer.",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        usuarioViewModel
                            .eliminarUsuario(
                                usuario
                            )

                        usuarioAEliminar =
                            null
                    }
                ) {

                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        usuarioAEliminar = null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            usuario.nombre,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            usuario.correo,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Card(
                    shape =
                        RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
                ) {

                    Text(
                        text =
                            usuario.tipoUsuario,
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    6.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                OutlinedButton(
                    onClick =
                        onEditClick
                ) {

                    Text("Editar")
                }

                Spacer(
                    modifier =
                        Modifier.padding(
                            horizontal = 4.dp
                        )
                )

                TextButton(
                    onClick =
                        onDeleteClick
                ) {

                    Text(
                        text = "Eliminar",
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        }
    }
}