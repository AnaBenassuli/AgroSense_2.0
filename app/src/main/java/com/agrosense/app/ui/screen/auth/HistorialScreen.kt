package com.agrosense.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(
    diagnosticoViewModel: DiagnosticoViewModel,
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

    LaunchedEffect(parcelas) {
        if (
            parcelaSeleccionada == null &&
            parcelas.isNotEmpty()
        ) {
            parcelaSeleccionada = parcelas.first()
        }
    }

    val flujoDiagnosticos =
        parcelaSeleccionada?.let {
            diagnosticoViewModel.observarPorParcela(it.id)
        } ?: flowOf(emptyList())

    val diagnosticos by flujoDiagnosticos.collectAsState(
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

        Spacer(
            modifier = Modifier.height(34.dp)
        )


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
                    text = "Historial",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Consulta tus diagnósticos anteriores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
            modifier = Modifier.height(22.dp)
        )

        when {

            parcelas.isEmpty() -> {

                EmptyHistoryCard(
                    title = "No tienes parcelas",
                    description =
                        "Agrega una parcela para comenzar a registrar diagnósticos."
                )
            }

            diagnosticos.isEmpty() -> {

                EmptyHistoryCard(
                    title = "Sin diagnósticos",
                    description =
                        "Esta parcela aún no tiene diagnósticos registrados."
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
                        text = "Diagnósticos",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${diagnosticos.size} registros",
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    items(
                        items = diagnosticos,
                        key = { diagnostico ->
                            diagnostico.id
                        }
                    ) { diagnostico ->

                        DiagnosticoHistoryCard(
                            diagnostico = diagnostico,
                            onDeleteClick = {
                                diagnosticoViewModel
                                    .eliminarDiagnostico(
                                        diagnostico
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
}

@Composable
private fun DiagnosticoHistoryCard(
    diagnostico: Diagnostico,
    onDeleteClick: () -> Unit
) {

    val bitmap = remember(
        diagnostico.imagenRuta
    ) {

        val archivo = File(
            diagnostico.imagenRuta
        )

        if (archivo.exists()) {
            BitmapFactory.decodeFile(
                archivo.absolutePath
            )
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {

        Column {

            bitmap?.let {

                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription =
                        "Imagen del diagnóstico",
                    contentScale =
                        ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )
            }

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
                            text = diagnostico.resultado,
                            style =
                                MaterialTheme.typography
                                    .titleLarge,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                formatearFecha(
                                    diagnostico.fecha
                                ),
                            style =
                                MaterialTheme.typography
                                    .bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Spacer(
                        modifier = Modifier.padding(6.dp)
                    )

                    ConfidenceCard(
                        confidence =
                            diagnostico.confianza
                    )
                }

                if (
                    !diagnostico.observaciones
                        .isNullOrBlank()
                ) {

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = "Observaciones",
                        style =
                            MaterialTheme.typography
                                .labelSmall,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            diagnostico.observaciones
                                ?: "",
                        style =
                            MaterialTheme.typography
                                .bodyMedium
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
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
                                MaterialTheme.colorScheme
                                    .error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceCard(
    confidence: Float
) {

    val porcentaje =
        (confidence * 100)
            .toInt()
            .coerceIn(
                0,
                100
            )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp
            ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "$porcentaje%",
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
            )

            Text(
                text = "Confianza",
                style =
                    MaterialTheme.typography
                        .labelSmall,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard(
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
                    MaterialTheme.typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = description,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

private fun formatearFecha(
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