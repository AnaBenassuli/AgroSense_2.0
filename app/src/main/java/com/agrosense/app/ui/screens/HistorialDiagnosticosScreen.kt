package com.agrosense.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Historial de diagnósticos de una parcela, leído de Room. */
@Composable
fun HistorialDiagnosticosScreen(navController: NavController, parcelaId: Long) {
    val context = LocalContext.current
    val diagnosticoViewModel: DiagnosticoViewModel = viewModel(factory = agroSenseViewModelFactory(context))

    val diagnosticos by diagnosticoViewModel.observarPorParcela(parcelaId)
        .collectAsStateWithLifecycle(emptyList())

    // Dentro de remember y no en una constante: el idioma puede cambiar con la
    // app abierta, y al hacerlo Android recrea la Activity y esto se reevalúa.
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Historial de diagnósticos", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Parcela $parcelaId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (diagnosticos.isEmpty()) {
            Text(
                "No hay diagnósticos registrados para esta parcela.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(diagnosticos, key = { it.id }) { diagnostico ->
                    TarjetaDiagnostico(
                        diagnostico = diagnostico,
                        formatoFecha = formatoFecha,
                        onClick = {
                            navController.navigate(
                                Route.ResultadoDiagnostico.create(diagnostico.id)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaDiagnostico(
    diagnostico: Diagnostico,
    formatoFecha: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = diagnostico.imagenRuta,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(diagnostico.resultado, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Confianza: ${porcentaje(diagnostico.confianza)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    formatoFecha.format(Date(diagnostico.fecha)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


