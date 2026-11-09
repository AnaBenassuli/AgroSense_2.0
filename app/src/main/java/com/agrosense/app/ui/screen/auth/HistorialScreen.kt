package com.agrosense.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.ml.NombresClases
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroChipButton
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroOverline
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroSelectField
import com.agrosense.app.ui.components.AgroStatCard
import com.agrosense.app.ui.components.AgroStatusChip
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.AgroTopBar
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
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

    AgroScreen {

        AgroTopBar(
            title = "Historial",
            onBack = onBackClick
        )

        Text(
            text = "Consulta tus diagnósticos anteriores.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SELECTOR DE PARCELA
        AgroSelectField(
            label = "Parcela",
            valueText =
                parcelaSeleccionada?.let {
                    "${it.nombre} · ${it.cultivo}"
                } ?: "Seleccionar parcela",
            options = parcelas,
            optionLabel = { "${it.nombre} · ${it.cultivo}" },
            onSelect = { parcelaSeleccionada = it },
            leadingIcon = R.drawable.ic_location,
            enabled = parcelas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {

            parcelas.isEmpty() -> {

                AgroEmptyState(
                    icon = R.drawable.ic_location,
                    title = "No tienes parcelas",
                    description =
                        "Agrega una parcela para comenzar a registrar diagnósticos."
                )
            }

            diagnosticos.isEmpty() -> {

                AgroEmptyState(
                    icon = R.drawable.ic_history,
                    title = "Sin diagnósticos",
                    description =
                        "Esta parcela aún no tiene diagnósticos registrados."
                )
            }

            else -> {

                AgroSectionHeader(
                    title = "Diagnósticos",
                    trailing =
                        if (diagnosticos.size == 1) {
                            "1 registro"
                        } else {
                            "${diagnosticos.size} registros"
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
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

    AgroCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {

        bitmap?.let {

            Box {

                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription =
                        "Imagen del diagnóstico",
                    contentScale =
                        ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                AgroStatusChip(
                    text = etiquetaModulo(diagnostico.modulo),
                    tone = AgroTone.ACCENT,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            if (bitmap == null) {

                AgroOverline(
                    text = etiquetaModulo(diagnostico.modulo),
                    color = AgroTextTertiary
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = NombresClases.legible(diagnostico.resultado),
                        style = MaterialTheme.typography.titleLarge,
                        color = AgroTextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = formatearFecha(diagnostico.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroTextTertiary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                ConfidenceCard(
                    confidence = diagnostico.confianza
                )
            }

            if (
                !diagnostico.observaciones
                    .isNullOrBlank()
            ) {

                Spacer(modifier = Modifier.height(14.dp))

                AgroOverline(text = "Observaciones")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = diagnostico.observaciones ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgroTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                AgroChipButton(
                    text = "Eliminar",
                    icon = R.drawable.ic_delete,
                    tone = AgroTone.ERROR,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

/** La confianza se colorea por nivel para leerla sin ver la cifra. */
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

    AgroStatCard(
        label = "Confianza",
        value = "$porcentaje%",
        tone = tonoConfianza(porcentaje)
    )
}

internal fun tonoConfianza(porcentaje: Int): AgroTone =
    when {
        porcentaje >= 70 -> AgroTone.SUCCESS
        porcentaje >= 40 -> AgroTone.WARNING
        else -> AgroTone.ERROR
    }

internal fun etiquetaModulo(modulo: Modulo): String =
    when (modulo) {
        Modulo.CULTIVO -> "Cultivo"
        Modulo.GANADO -> "Ganado"
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