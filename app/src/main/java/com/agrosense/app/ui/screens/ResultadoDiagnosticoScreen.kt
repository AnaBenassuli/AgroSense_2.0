package com.agrosense.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.ml.porcentaje
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory

/**
 * Resultado de un diagnóstico ya guardado en Room.
 *
 * El diagnóstico se inserta al analizar la foto, no aquí: la ruta lo
 * direcciona por id, así que para llegar a esta pantalla la fila ya tiene
 * que existir. Lo que se guarda desde aquí son las observaciones del
 * usuario sobre ese diagnóstico.
 */
@Composable
fun ResultadoDiagnosticoScreen(navController: NavController, diagnosticoId: Long) {
    val context = LocalContext.current
    val diagnosticoViewModel: DiagnosticoViewModel = viewModel(factory = agroSenseViewModelFactory(context))

    var diagnostico by remember { mutableStateOf<Diagnostico?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var observaciones by remember { mutableStateOf("") }
    var guardado by remember { mutableStateOf(false) }

    LaunchedEffect(diagnosticoId) {
        val cargado = diagnosticoViewModel.obtenerPorId(diagnosticoId)
        diagnostico = cargado
        observaciones = cargado?.observaciones.orEmpty()
        cargando = false
    }

    val actual = diagnostico

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resultado del análisis", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        when {
            cargando -> Text("Cargando…", style = MaterialTheme.typography.bodyMedium)

            actual == null -> Text(
                "No se encontró el diagnóstico $diagnosticoId.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            else -> {
                AsyncImage(
                    model = actual.imagenRuta,
                    contentDescription = "Foto analizada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(actual.resultado, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Confianza: ${porcentaje(actual.confianza)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val alternativas = actual.top3
                        if (alternativas.size > 1) {
                            Spacer(Modifier.height(12.dp))
                            Text("Otras posibilidades", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            alternativas.forEach { prediccion ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            prediccion.clase,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            prediccion.porcentaje(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { prediccion.confianza },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = {
                        observaciones = it
                        guardado = false
                    },
                    label = { Text("Observaciones") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        diagnosticoViewModel.actualizarDiagnostico(actual.copy(observaciones = observaciones.trim()))
                        diagnostico = actual.copy(observaciones = observaciones.trim())
                        guardado = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (guardado) "Observaciones guardadas" else "Guardar observaciones")
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Este es un diagnóstico asistido y no reemplaza la opinión de " +
                        "un agrónomo o veterinario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            Route.HistorialDiagnosticos.create(actual.parcelaId)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver historial de la parcela")
                }

                TextButton(
                    onClick = {
                        navController.navigate(
                            Route.CapturaDiagnostico.create(actual.parcelaId)
                        ) {
                            popUpTo(Route.Home.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Capturar otra foto")
                }
            }
        }
    }
}

internal fun porcentaje(valor: Float): String = "%.1f%%".format(valor * 100)
