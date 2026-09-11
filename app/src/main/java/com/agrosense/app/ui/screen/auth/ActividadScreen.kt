package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun ActividadScreen(
    actividadViewModel: ActividadViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit
) {

    val parcelas by parcelaViewModel.parcelas.collectAsState()

    var parcelaSeleccionada by remember {
        mutableStateOf<Parcela?>(null)
    }

    var menuParcelasAbierto by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var tipo by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    val actividadesFlow =
        parcelaSeleccionada?.let {
            actividadViewModel.observarPorParcela(it.id)
        } ?: flowOf(emptyList())

    val actividades by actividadesFlow.collectAsState(
        initial = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Actividades",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Registra y consulta las actividades realizadas en cada parcela.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        OutlinedButton(
            onClick = {
                menuParcelasAbierto = true
            },
            enabled = parcelas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                parcelaSeleccionada?.let {
                    "${it.nombre} · ${it.cultivo}"
                } ?: "Seleccionar parcela"
            )
        }

        DropdownMenu(
            expanded = menuParcelasAbierto,
            onDismissRequest = {
                menuParcelasAbierto = false
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
                        parcelaSeleccionada = parcela
                        menuParcelasAbierto = false
                    }
                )
            }
        }

        if (parcelas.isEmpty()) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Primero registra una parcela.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                mostrarFormulario = true
            },
            enabled = parcelaSeleccionada != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar actividad")
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (parcelaSeleccionada != null) {

            if (actividades.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Sin actividades",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Todavía no hay actividades registradas para esta parcela."
                        )
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = actividades,
                        key = { actividad ->
                            actividad.id
                        }
                    ) { actividad ->

                        ActividadCard(
                            actividad = actividad,
                            onDeleteClick = {
                                actividadViewModel
                                    .eliminarActividad(
                                        actividad
                                    )
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                mostrarFormulario = false
            },

            title = {
                Text("Nueva actividad")
            },

            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    OutlinedTextField(
                        value = tipo,
                        onValueChange = { nuevoTipo ->
                            tipo = nuevoTipo
                        },
                        label = {
                            Text("Tipo de actividad")
                        },
                        placeholder = {
                            Text(
                                "Ej. Riego, fertilización, fumigación"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { nuevaDescripcion ->
                            descripcion = nuevaDescripcion
                        },
                        label = {
                            Text("Descripción")
                        },
                        placeholder = {
                            Text(
                                "Ej. Riego por goteo durante 30 minutos"
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val parcela =
                            parcelaSeleccionada

                        if (
                            parcela != null &&
                            tipo.isNotBlank() &&
                            descripcion.isNotBlank()
                        ) {

                            val actividad =
                                Actividad(
                                    parcelaId =
                                        parcela.id,
                                    tipo =
                                        tipo.trim(),
                                    descripcion =
                                        descripcion.trim()
                                )

                            actividadViewModel
                                .registrarActividad(
                                    actividad
                                )

                            tipo = ""
                            descripcion = ""

                            mostrarFormulario =
                                false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarFormulario = false
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
    onDeleteClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = actividad.tipo,
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = actividad.descripcion
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Fecha: ${
                        formatearFechaActividad(
                            actividad.fecha
                        )
                    }",
                style =
                    MaterialTheme.typography.bodySmall
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            TextButton(
                onClick = onDeleteClick
            ) {
                Text("Eliminar")
            }
        }
    }
}

private fun formatearFechaActividad(
    timestamp: Long
): String {

    val formato =
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

    return formato.format(
        Date(timestamp)
    )
}