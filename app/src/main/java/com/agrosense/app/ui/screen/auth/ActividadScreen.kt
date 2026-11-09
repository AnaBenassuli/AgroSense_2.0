package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.ui.components.AgroBanner
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroCardActions
import com.agrosense.app.ui.components.AgroDialogo
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroEncabezado
import com.agrosense.app.ui.components.AgroIconBadge
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroSelectField
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTonalButton
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.Avisos
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel

// Labores más comunes en cultivos y ganado, para elegir con un toque en el formulario
private val TIPOS_ACTIVIDAD = listOf(
    "Riego",
    "Fertilización",
    "Fumigación",
    "Deshierbe",
    "Cosecha",
    "Vacunación",
    "Desparasitación"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActividadScreen(
    actividadViewModel: ActividadViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit,
    onIrAParcelas: () -> Unit = {}
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

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    fun limpiarFormulario() {
        tipo = ""
        descripcion = ""
        actividadEditando = null
        mensajeError = null
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
        // CREATE: botón principal fijo abajo
        bottomBar = {
            AgroPrimaryButton(
                text = "Registrar actividad",
                icon = R.drawable.ic_add,
                onClick = { nuevaActividad() },
                enabled = parcelaSeleccionadaId != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        AgroEncabezado(
            titulo = "Actividades",
            descripcion = "Lleva el registro de lo que haces en cada parcela: riegos, fertilizaciones, " +
                    "fumigaciones, vacunas del ganado, cosechas…",
            onBack = onBackClick
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
                description = "Las actividades se anotan dentro de una parcela (tu terreno o potrero). " +
                        "Registra una y vuelve aquí."
            )

            Spacer(modifier = Modifier.height(12.dp))

            AgroTonalButton(
                text = "Ir a Mis parcelas",
                icon = R.drawable.ic_arrow_forward,
                onClick = onIrAParcelas,
                modifier = Modifier.fillMaxWidth()
            )

        } else if (actividades.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_event,
                title = "Sin actividades en esta parcela",
                description = "Toca \"Registrar actividad\" cada vez que riegues, fertilices, " +
                        "fumigues o vacunes. Por ejemplo: Riego — 2 horas por goteo en la mañana."
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

        AgroDialogo(
            onDismissRequest = {
                mostrarFormulario = false
                limpiarFormulario()
            },

            shape = RoundedCornerShape(20.dp),

            title = {

                Text(
                    text =
                        if (actividadEditando == null) {
                            "Nueva actividad"
                        } else {
                            "Editar actividad"
                        },
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text = "¿Qué hiciste?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tipos comunes a un toque; si no está, se escribe abajo
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        TIPOS_ACTIVIDAD.forEach { opcion ->

                            FilterChip(
                                selected = tipo == opcion,
                                onClick = {
                                    tipo = opcion
                                },
                                label = {
                                    Text(opcion)
                                }
                            )
                        }
                    }

                    FieldSpacer()

                    AgroTextField(
                        value = tipo,
                        onValueChange = { tipo = it },
                        label = "Tipo de actividad",
                        placeholder = "Elige uno arriba o escríbelo",
                        leadingIcon = R.drawable.ic_event,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FieldSpacer()

                    AgroTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = "Descripción",
                        placeholder = "Ej. 2 horas por goteo; 1 L de fertilizante por surco",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
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

                        val parcelaId =
                            parcelaSeleccionadaId

                        mensajeError = when {
                            parcelaId == null -> "Elige la parcela de la actividad."
                            tipo.isBlank() -> "Elige qué hiciste o escríbelo."
                            descripcion.isBlank() -> "Describe la actividad (por ejemplo, cuánto tiempo o cuánto producto)."
                            else -> null
                        }

                        if (
                            mensajeError == null &&
                            parcelaId != null
                        ) {

                            val actual =
                                actividadEditando

                            if (actual == null) {

                                // CREATE
                                actividadViewModel
                                    .registrarActividad(
                                        parcelaId = parcelaId,
                                        tipo = tipo.trim(),
                                        descripcion = descripcion.trim()
                                    )

                            } else {

                                // UPDATE
                                actividadViewModel
                                    .actualizarActividad(
                                        actual.copy(
                                            tipo = tipo.trim(),
                                            descripcion = descripcion.trim()
                                        )
                                    )
                            }

                            Avisos.mostrar(if (actual == null) "Actividad guardada" else "Actividad actualizada")

                            mostrarFormulario = false

                            limpiarFormulario()
                        }
                    }
                ) {

                    Text(
                        if (actividadEditando == null) {
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

    // =====================================================
    // DELETE
    // =====================================================

    actividadAEliminar?.let { actividad ->

        AgroDialogo(
            onDismissRequest = {
                actividadAEliminar = null
            },

            shape = RoundedCornerShape(20.dp),

            title = {

                Text(
                    text = "Eliminar actividad",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text = "¿Deseas eliminar la actividad \"${actividad.tipo}\"?"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                        Avisos.mostrar("Actividad eliminada")

                        actividadViewModel
                            .eliminarActividad(
                                actividad
                            )

                        actividadAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        actividadAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
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

            Text(
                text = actividad.tipo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroTextPrimary,
                modifier = Modifier.weight(1f)
            )
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