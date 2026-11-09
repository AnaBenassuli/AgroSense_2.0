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
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroCardActions
import com.agrosense.app.ui.components.AgroDialog
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroIconBadge
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroSelectField
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTopBar
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActividadScreen(
    actividadViewModel: ActividadViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit
) {

    val parcelas by parcelaViewModel.parcelas.collectAsState()

    var parcelaSeleccionadaId by remember {
        mutableStateOf<Long?>(null)
    }

    if (
        parcelaSeleccionadaId == null &&
        parcelas.isNotEmpty()
    ) {
        parcelaSeleccionadaId = parcelas.first().id
    }

    val actividades = parcelaSeleccionadaId?.let { id ->

        val flow = remember(id) {
            actividadViewModel.observarPorParcela(id)
        }

        flow.collectAsState(
            initial = emptyList()
        ).value

    } ?: emptyList()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var actividadEditando by remember {
        mutableStateOf<Actividad?>(null)
    }

    var actividadAEliminar by remember {
        mutableStateOf<Actividad?>(null)
    }

    var tipo by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    fun limpiarFormulario() {
        tipo = ""
        descripcion = ""
        actividadEditando = null
    }

    fun nuevaActividad() {
        limpiarFormulario()
        mostrarFormulario = true
    }

    fun editarActividad(
        actividad: Actividad
    ) {
        actividadEditando = actividad
        tipo = actividad.tipo
        descripcion = actividad.descripcion
        mostrarFormulario = true
    }

    AgroScreen(
        bottomBar = {
            // CREATE
            AgroPrimaryButton(
                text = "Registrar actividad",
                icon = R.drawable.ic_add,
                onClick = { nuevaActividad() },
                enabled = parcelaSeleccionadaId != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        AgroTopBar(
            title = "Actividades",
            onBack = onBackClick
        )

        Text(
            text = "Registra y administra las actividades de tus parcelas.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SELECTOR DE PARCELA
        AgroSelectField(
            label = "Parcela",
            valueText =
                parcelas.firstOrNull {
                    it.id == parcelaSeleccionadaId
                }?.let {
                    "${it.nombre} · ${it.cultivo}"
                } ?: "Seleccionar parcela",
            options = parcelas,
            optionLabel = { "${it.nombre} · ${it.cultivo}" },
            onSelect = { parcelaSeleccionadaId = it.id },
            leadingIcon = R.drawable.ic_location,
            enabled = parcelas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (parcelas.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_location,
                title = "Primero agrega una parcela",
                description = "Necesitas una parcela para registrar actividades."
            )

        } else if (actividades.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_event,
                title = "Sin actividades",
                description = "Todavía no hay actividades registradas para esta parcela."
            )

        } else {

            AgroSectionHeader(
                title = "Registro de actividades",
                trailing =
                    if (actividades.size == 1) {
                        "1 registrada"
                    } else {
                        "${actividades.size} registradas"
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // READ
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(
                    items = actividades,
                    key = {
                        it.id
                    }
                ) { actividad ->

                    ActividadCard(
                        actividad = actividad,
                        onEditClick = {
                            editarActividad(
                                actividad
                            )
                        },
                        onDeleteClick = {
                            actividadAEliminar =
                                actividad
                        }
                    )
                }
            }
        }
    }

    // =====================================================
    // CREATE / UPDATE
    // =====================================================

    if (mostrarFormulario) {

        val formularioCompleto =
            tipo.isNotBlank() &&
                    descripcion.isNotBlank()

        AgroDialog(
            title =
                if (actividadEditando == null) {
                    "Nueva actividad"
                } else {
                    "Editar actividad"
                },
            confirmText =
                if (actividadEditando == null) {
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

                val parcelaId =
                    parcelaSeleccionadaId

                if (
                    parcelaId != null &&
                    formularioCompleto
                ) {

                    val actual =
                        actividadEditando

                    if (actual == null) {

                        // CREATE
                        actividadViewModel
                            .registrarActividad(
                                parcelaId =
                                    parcelaId,
                                tipo =
                                    tipo.trim(),
                                descripcion =
                                    descripcion.trim()
                            )

                    } else {

                        // UPDATE
                        actividadViewModel
                            .actualizarActividad(
                                actual.copy(
                                    tipo =
                                        tipo.trim(),
                                    descripcion =
                                        descripcion.trim()
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
                value = tipo,
                onValueChange = { tipo = it },
                label = "Tipo de actividad",
                placeholder = "Ej. Riego, fertilización...",
                leadingIcon = R.drawable.ic_event,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            AgroTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción",
                placeholder = "Describe la actividad realizada",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    actividadAEliminar?.let { actividad ->

        AgroDialog(
            title = "Eliminar actividad",
            confirmText = "Eliminar",
            destructive = true,
            onDismiss = {
                actividadAEliminar = null
            },
            onConfirm = {
                actividadViewModel
                    .eliminarActividad(
                        actividad
                    )

                actividadAEliminar =
                    null
            }
        ) {

            Text(
                text = "¿Deseas eliminar la actividad \"${actividad.tipo}\"?",
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
private fun ActividadCard(
    actividad: Actividad,
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

            AgroIconBadge(
                icon = R.drawable.ic_event,
                size = 42.dp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = actividad.tipo,
                    style = MaterialTheme.typography.titleMedium,
                    color = AgroTextPrimary
                )

                Text(
                    text = formatearFechaActividad(actividad.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroTextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = actividad.descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        AgroCardActions(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

private fun formatearFechaActividad(
    timestamp: Long
): String {

    val formato =
        SimpleDateFormat(
            "dd MMM yyyy · HH:mm",
            Locale.getDefault()
        )

    return formato.format(
        Date(timestamp)
    )
}