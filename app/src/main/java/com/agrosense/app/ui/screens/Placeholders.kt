package com.agrosense.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Andamiaje temporal para las pantallas que todavía no se han
 * implementado. Cada una se reemplaza en el bloque que le corresponde.
 */
@Composable
internal fun PantallaEnConstruccion(titulo: String, detalle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        if (detalle != null) {
            Text(detalle, style = MaterialTheme.typography.bodyMedium)
        }
        Text("En construcción", style = MaterialTheme.typography.bodySmall)
    }
}
