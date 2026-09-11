package com.agrosense.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    val diagnosticosFlow =
        parcelaSeleccionada?.let {
            diagnosticoViewModel.observarPorParcela(it.id)
        } ?: flowOf(emptyList())

    val diagnosticos by diagnosticosFlow.collectAsState(
        initial = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Historial",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Consulta los diagnósticos registrados por parcela.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedButton(
            onClick = {
                menuAbierto = true
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
            expanded = menuAbierto,
            onDismissRequest = {
                menuAbierto = false
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
                        menuAbierto = false
                    }
                )
            }
        }

        if (parcelas.isEmpty()) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Primero registra una parcela.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (parcelaSeleccionada != null) {

            if (diagnosticos.isEmpty()) {

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
                            text = "Sin diagnósticos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Todavía no hay diagnósticos registrados para esta parcela."
                        )
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = diagnosticos,
                        key = { diagnostico ->
                            diagnostico.id
                        }
                    ) { diagnostico ->

                        DiagnosticoCard(
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

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
private fun DiagnosticoCard(
    diagnostico: Diagnostico,
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

            val archivo = File(
                diagnostico.imagenRuta
            )

            if (archivo.exists()) {

                val bitmap = remember(
                    diagnostico.imagenRuta
                ) {
                    BitmapFactory.decodeFile(
                        diagnostico.imagenRuta
                    )
                }

                if (bitmap != null) {

                    Image(
                        bitmap =
                            bitmap.asImageBitmap(),
                        contentDescription =
                            "Imagen del diagnóstico",
                        contentScale =
                            ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )
                }
            }

            Text(
                text = diagnostico.resultado,
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Confianza: ${
                        (diagnostico.confianza * 100).toInt()
                    }%"
            )

            Text(
                text =
                    "Fecha: ${
                        formatearFecha(
                            diagnostico.fecha
                        )
                    }"
            )

            diagnostico.observaciones?.let {

                if (it.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Observaciones: $it"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            TextButton(
                onClick = onDeleteClick
            ) {
                Text("Eliminar")
            }
        }
    }
}

private fun formatearFecha(
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