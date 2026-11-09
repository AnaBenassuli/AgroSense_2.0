package com.agrosense.app.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Botón para volver a la pantalla de inicio.
 *
 * Antes era una flecha "←" suelta: en las pruebas con usuarios nuevos era muy pequeña y no se
 * entendía que servía para regresar. Ahora tiene texto y un área táctil de al menos 48 dp, que es
 * el mínimo recomendado por Android para que se pueda tocar con el dedo sin fallar.
 */
@Composable
fun BotonRegresar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String = "Inicio"
) {

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp)
    ) {

        Text(
            text = "←  $texto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
