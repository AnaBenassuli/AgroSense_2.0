package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.agrosense.app.ui.components.AgroDialogo
import com.agrosense.app.ui.components.Avisos
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import com.agrosense.app.ui.components.BotonRegresar
import com.agrosense.app.data.local.entity.Actividad
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

    var mostrarMenuParcelas by remember {
        mutableStateOf(false)
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
    ) {

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
                text = "Actividades",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Lleva el registro de lo que haces en cada parcela: riegos, fertilizaciones, " +
                "fumigaciones, vacunas del ganado, cosechas…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // SELECTOR DE PARCELA

        Text(
            text = "Parcela",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Column {

            OutlinedButton(
                onClick = {
                    mostrarMenuParcelas = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                val nombreParcela =
                    parcelas.firstOrNull {
                        it.id == parcelaSeleccionadaId
                    }?.nombre
                        ?: "Seleccionar parcela"

                Text(nombreParcela)
            }

            DropdownMenu(
                expanded = mostrarMenuParcelas,
                onDismissRequest = {
                    mostrarMenuParcelas = false
                }
            ) {

                parcelas.forEach { parcela ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                "${parcela.nombre} · ${parcela.cultivo}"
                            )
                        },
                        onClick = {
                            parcelaSeleccionadaId =
                                parcela.id

                            mostrarMenuParcelas =
                                false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // CREATE
        Button(
            onClick = {
                nuevaActividad()
            },
            enabled =
                parcelaSeleccionadaId != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text("+ Registrar actividad")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (parcelas.isEmpty()) {

            EmptyActividadCard(
                titulo = "Primero agrega una parcela",
                descripcion = "Las actividades se anotan dentro de una parcela (tu terreno o potrero). " +
                    "Registra una y vuelve aquí."
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onIrAParcelas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ir a Mis parcelas")
            }

        } else if (actividades.isEmpty()) {

            EmptyActividadCard(
                titulo = "Sin actividades en esta parcela",
                descripcion = "Toca \"+ Registrar actividad\" cada vez que riegues, fertilices, " +
                    "fumigues o vacunes. Por ejemplo: Riego — 2 horas por goteo en la mañana."
            )

        } else {

            Text(
                text = if (actividades.size == 1) {
                    "1 actividad registrada"
                } else {
                    "${actividades.size} actividades registradas"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // READ
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
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

            shape = RoundedCornerShape(18.dp),

            title = {

                Text(
                    text =
                        if (actividadEditando == null) {
                            "Nueva actividad"
                        } else {
                            "Editar actividad"
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text = "¿Qué hiciste?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

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

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {
                            tipo = it
                        },
                        label = {
                            Text(
                                "Tipo de actividad"
                            )
                        },
                        placeholder = {
                            Text(
                                "Elige uno arriba o escríbelo"
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

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it
                        },
                        label = {
                            Text("Descripción")
                        },
                        placeholder = {
                            Text(
                                "Ej. 2 horas por goteo; 1 L de fertilizante por surco"
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        singleLine = false,
                        modifier =
                            Modifier.fillMaxWidth()
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
                            Avisos.mostrar(if (actual == null) "Actividad guardada" else "Actividad actualizada")

                            mostrarFormulario =
                                false

                            limpiarFormulario()
                        }
                    }
                ) {

                    Text(
                        if (
                            actividadEditando == null
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

    // =====================================================
    // DELETE
    // =====================================================

    actividadAEliminar?.let { actividad ->

        AgroDialogo(
            onDismissRequest = {
                actividadAEliminar = null
            },

            shape = RoundedCornerShape(18.dp),

            title = {

                Text(
                    text = "Eliminar actividad",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "¿Deseas eliminar la actividad \"${actividad.tipo}\"?"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Esta acción no se puede deshacer.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {
                        Avisos.mostrar("Actividad eliminada")

                        actividadViewModel
                            .eliminarActividad(
                                actividad
                            )

                        actividadAEliminar =
                            null
                    }
                ) {

                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        actividadAEliminar =
                            null
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text = actividad.tipo,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = actividad.descripcion,
                style =
                    MaterialTheme.typography.bodyMedium
            )

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
                    onClick = onEditClick
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
                    onClick = onDeleteClick
                ) {

                    Text(
                        text = "Eliminar",
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActividadCard(
    titulo: String,
    descripcion: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                text = titulo,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = descripcion,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}