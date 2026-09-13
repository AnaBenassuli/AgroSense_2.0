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
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.ui.viewmodel.ActividadViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import kotlinx.coroutines.flow.flowOf
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

    LaunchedEffect(parcelas) {
        if (
            parcelaSeleccionada == null &&
            parcelas.isNotEmpty()
        ) {
            parcelaSeleccionada = parcelas.first()
        }
    }

    val flujoActividades =
        parcelaSeleccionada?.let {
            actividadViewModel.observarPorParcela(it.id)
        } ?: flowOf(emptyList())

    val actividades by flujoActividades.collectAsState(
        initial = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
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

            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Column {

                Text(
                    text = "Actividades",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Lleva el control de las labores de tu cultivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // PARCELA

        Text(
            text = "Parcela",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {
                menuParcelasAbierto = true
            },
            enabled = parcelas.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text =
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

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // NUEVA ACTIVIDAD

        Button(
            onClick = {
                mostrarFormulario = true
            },
            enabled = parcelaSeleccionada != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {

            Text(
                text = "+ Registrar actividad",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        when {

            parcelas.isEmpty() -> {

                EmptyActivityCard(
                    title = "No tienes parcelas",
                    description =
                        "Primero agrega una parcela para registrar actividades."
                )
            }

            actividades.isEmpty() -> {

                EmptyActivityCard(
                    title = "Sin actividades",
                    description =
                        "Aún no has registrado labores para esta parcela."
                )
            }

            else -> {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "Actividad reciente",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${actividades.size} registros",
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

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

                    item {
                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }

    // FORMULARIO

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                mostrarFormulario = false
            },

            shape = RoundedCornerShape(18.dp),

            title = {
                Text(
                    text = "Nueva actividad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    parcelaSeleccionada?.let { parcela ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {

                                Text(
                                    text = parcela.nombre,
                                    style =
                                        MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onPrimaryContainer
                                )

                                Text(
                                    text = parcela.cultivo,
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        MaterialTheme.colorScheme
                                            .onPrimaryContainer
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )
                    }

                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {
                            tipo = it
                        },
                        label = {
                            Text("Tipo de actividad")
                        },
                        placeholder = {
                            Text("Ej. Riego, fertilización, poda...")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
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
                                "Describe el trabajo realizado..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        minLines = 4,
                        maxLines = 6,
                        singleLine = false,
                        shape = RoundedCornerShape(14.dp)
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

                            actividadViewModel
                                .registrarActividad(
                                    Actividad(
                                        parcelaId = parcela.id,
                                        tipo = tipo.trim(),
                                        descripcion =
                                            descripcion.trim()
                                    )
                                )

                            tipo = ""
                            descripcion = ""
                            mostrarFormulario = false
                        }
                    },
                    enabled =
                        tipo.isNotBlank() &&
                                descripcion.isNotBlank()
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = actividad.tipo,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = formatearFechaActividad(
                            actividad.fecha
                        ),
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Text(
                        text = "Registrada",
                        style =
                            MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Descripción",
                style =
                    MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = actividad.descripcion,
                style =
                    MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

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
private fun EmptyActivityCard(
    title: String,
    description: String
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
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = description,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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