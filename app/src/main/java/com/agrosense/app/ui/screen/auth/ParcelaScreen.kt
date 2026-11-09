package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.agrosense.app.ui.components.AgroDialogo
import com.agrosense.app.ui.components.Avisos
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.BotonRegresar
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.repository.ContenidoParcela
import com.agrosense.app.ui.viewmodel.ParcelaViewModel

@Composable
fun ParcelaScreen(
    viewModel: ParcelaViewModel,
    onBackClick: () -> Unit
) {

    val parcelas by viewModel.parcelas.collectAsState()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    // Si es null = estamos creando.
    // Si tiene una parcela = estamos editando.
    var parcelaEditando by remember {
        mutableStateOf<Parcela?>(null)
    }

    // Parcela que se quiere eliminar.
    var parcelaAEliminar by remember {
        mutableStateOf<Parcela?>(null)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var ubicacion by remember {
        mutableStateOf("")
    }

    var cultivo by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    // Lo que se borra junto con la parcela (Room borra en cascada), para decirlo en el aviso
    var contenidoAEliminar by remember {
        mutableStateOf<ContenidoParcela?>(null)
    }

    LaunchedEffect(parcelaAEliminar?.id) {
        contenidoAEliminar = parcelaAEliminar?.let { viewModel.contarContenido(it.id) }
    }

    fun limpiarFormulario() {
        nombre = ""
        ubicacion = ""
        cultivo = ""
        parcelaEditando = null
        mensajeError = null
    }

    fun abrirFormularioNuevo() {
        limpiarFormulario()
        mostrarFormulario = true
    }

    fun abrirFormularioEditar(parcela: Parcela) {
        parcelaEditando = parcela
        nombre = parcela.nombre
        ubicacion = parcela.ubicacion
        cultivo = parcela.cultivo
        mostrarFormulario = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {

        Spacer(
            modifier = Modifier.height(34.dp)
        )

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BotonRegresar(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = "Mis parcelas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Administra tus cultivos y consulta su información.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // CREATE
        Button(
            onClick = {
                abrirFormularioNuevo()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "+ Agregar parcela",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // READ
        if (parcelas.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Aún no tienes parcelas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Agrega tu primera parcela para comenzar a registrar diagnósticos y actividades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            Text(
                text = if (parcelas.size == 1) {
                    "1 parcela registrada"
                } else {
                    "${parcelas.size} parcelas registradas"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = parcelas,
                    key = { parcela ->
                        parcela.id
                    }
                ) { parcela ->

                    ParcelaCard(
                        parcela = parcela,

                        // UPDATE
                        onEditClick = {
                            abrirFormularioEditar(parcela)
                        },

                        // DELETE
                        onDeleteClick = {
                            parcelaAEliminar = parcela
                        }
                    )
                }
            }
        }
    }

    // =========================================================
    // FORMULARIO CREATE / UPDATE
    // =========================================================

    if (mostrarFormulario) {

        AgroDialogo(
            onDismissRequest = {
                mostrarFormulario = false
                limpiarFormulario()
            },

            shape = RoundedCornerShape(18.dp),

            title = {
                Text(
                    text = if (parcelaEditando == null) {
                        "Nueva parcela"
                    } else {
                        "Editar parcela"
                    },
                    fontWeight = FontWeight.Bold
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
                            Text("Nombre de la parcela")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = cultivo,
                        onValueChange = {
                            cultivo = it
                        },
                        label = {
                            Text("Cultivo")
                        },
                        placeholder = {
                            Text("Ej. Maíz")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = {
                            ubicacion = it
                        },
                        label = {
                            Text("Ubicación")
                        },
                        placeholder = {
                            Text("Ej. Chihuahua")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    mensajeError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        mensajeError = when {
                            nombre.isBlank() -> "Escribe el nombre de la parcela."
                            cultivo.isBlank() -> "Escribe qué tienes en la parcela (cultivo o ganado)."
                            ubicacion.isBlank() -> "Escribe dónde está la parcela."
                            else -> null
                        }

                        if (mensajeError == null) {

                            val parcelaActual = parcelaEditando

                            if (parcelaActual == null) {

                                // CREATE
                                viewModel.registrarParcela(
                                    nombre = nombre.trim(),
                                    ubicacion = ubicacion.trim(),
                                    cultivo = cultivo.trim()
                                )

                            } else {

                                // UPDATE
                                val parcelaActualizada = parcelaActual.copy(
                                    nombre = nombre.trim(),
                                    ubicacion = ubicacion.trim(),
                                    cultivo = cultivo.trim()
                                )

                                viewModel.actualizarParcela(
                                    parcelaActualizada
                                )
                            }

                            Avisos.mostrar(if (parcelaActual == null) "Parcela guardada" else "Parcela actualizada")

                            mostrarFormulario = false
                            limpiarFormulario()
                        }
                    }
                ) {
                    Text(
                        text = if (parcelaEditando == null) {
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
                        mostrarFormulario = false
                        limpiarFormulario()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // =========================================================
    // CONFIRMACIÓN DELETE
    // =========================================================

    parcelaAEliminar?.let { parcela ->

        AgroDialogo(
            onDismissRequest = {
                parcelaAEliminar = null
            },

            shape = RoundedCornerShape(18.dp),

            title = {
                Text(
                    text = "Eliminar parcela",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Column {

                    Text(
                        text = "¿Estás seguro de que deseas eliminar \"${parcela.nombre}\"?"
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    contenidoAEliminar?.let { contenido ->

                        if (contenido.diagnosticos > 0 || contenido.actividades > 0) {

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "También se borrarán " +
                                    "${contenido.diagnosticos} diagnóstico(s) con sus fotos y " +
                                    "${contenido.actividades} actividad(es) de esta parcela.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        Avisos.mostrar("Parcela eliminada")
                        viewModel.eliminarParcela(parcela)

                        parcelaAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        parcelaAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ParcelaCard(
    parcela: Parcela,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = parcela.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = parcela.cultivo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Text(
                        text = "Activa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = parcela.ubicacion,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // UPDATE + DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedButton(
                    onClick = onEditClick
                ) {
                    Text("Editar")
                }

                Spacer(
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                TextButton(
                    onClick = onDeleteClick
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}