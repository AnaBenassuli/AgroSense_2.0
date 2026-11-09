package com.agrosense.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary

/**
 * Encabezado común de las pantallas internas: bolita verde para regresar,
 * título y una descripción corta debajo.
 */
@Composable
fun AgroEncabezado(
    titulo: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            BotonRegresar(
                onClick = onBack
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgroTextPrimary
            )
        }

        if (descripcion != null) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextSecondary
            )
        }
    }
}