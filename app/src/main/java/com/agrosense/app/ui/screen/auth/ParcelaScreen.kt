package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.repository.ContenidoParcela
import com.agrosense.app.ui.components.AgroBanner
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroCardActions
import com.agrosense.app.ui.components.AgroDataPill
import com.agrosense.app.ui.components.AgroDialogo
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroEncabezado
import com.agrosense.app.ui.components.AgroIconBadge
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroStatusChip
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.Avisos
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroTextPrimary
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

    AgroScreen(
        // CREATE: botón principal fijo abajo
        bottomBar = {
            AgroPrimaryButton(
                text = "Agregar parcela",
                icon = R.drawable.ic_add,
                onClick = { abrirFormularioNuevo() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        AgroEncabezado(
            titulo = "Mis parcelas",
            descripcion = "Administra tus cultivos y consulta su información.",
            onBack = onBackClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // READ
        if (parcelas.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_leaf,
                title = "Aún no tienes parcelas",
                description = "Agrega tu primera parcela para comenzar a registrar diagnósticos y actividades."
            )

        } else {

            AgroSectionHeader(
                title = "Tus parcelas",
                trailing =
                    if (parcelas.size == 1) {
                        "1 registrada"
                    } else {
                        "${parcelas.size} registradas"
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
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

            shape = RoundedCornerShape(20.dp),

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

                    AgroTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = "Nombre de la parcela",
                        placeholder = "Ej. Lote norte",
                        modifier = Modifier.fillMaxWidth()
                    )

                    FieldSpacer()

                    AgroTextField(
                        value = cultivo,
                        onValueChange = { cultivo = it },
                        label = "Cultivo",
                        placeholder = "Ej. Maíz",
                        leadingIcon = R.drawable.ic_leaf,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FieldSpacer()

                    AgroTextField(
                        value = ubicacion,
                        onValueChange = { ubicacion = it },
                        label = "Ubicación",
                        placeholder = "Ej. Chihuahua",
                        leadingIcon = R.drawable.ic_location,
                        modifier = Modifier.fillMaxWidth()
                    )

                    mensajeError?.let { error ->

                        Spacer(modifier = Modifier.height(12.dp))

                        AgroBanner(
                            text = error,
                            tone = AgroTone.ERROR
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    shape = RoundedCornerShape(12.dp),
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

            shape = RoundedCornerShape(20.dp),

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

                    contenidoAEliminar?.let { contenido ->

                        if (contenido.diagnosticos > 0 || contenido.actividades > 0) {

                            Spacer(modifier = Modifier.height(12.dp))

                            AgroBanner(
                                text = "También se borrarán " +
                                        "${contenido.diagnosticos} diagnóstico(s) con sus fotos y " +
                                        "${contenido.actividades} actividad(es) de esta parcela.",
                                tone = AgroTone.ERROR
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    shape = RoundedCornerShape(12.dp),
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

    AgroCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Icono + nombre + estado
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AgroIconBadge(
                icon = R.drawable.ic_leaf,
                size = 42.dp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = parcela.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            AgroStatusChip(
                text = "Activa",
                tone = AgroTone.SUCCESS,
                showDot = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Datos en pastillas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            AgroDataPill(
                label = "Cultivo",
                value = parcela.cultivo,
                modifier = Modifier.weight(1f)
            )

            AgroDataPill(
                label = "Ubicación",
                value = parcela.ubicacion,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // UPDATE + DELETE
        AgroCardActions(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}