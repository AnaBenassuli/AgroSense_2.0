package com.agrosense.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.theme.AgroGreen

/**
 * Botón para volver a la pantalla de inicio: una bolita verde con una flecha blanca.
 *
 * Mide 48 dp, el mínimo recomendado por Android para tocarlo con el dedo sin fallar.
 * [texto] ya no se ve, pero lo lee el lector de pantalla ("Volver a Inicio").
 */
@Composable
fun BotonRegresar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String = "Inicio"
) {

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(AgroGreen)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Volver a $texto" },
        contentAlignment = Alignment.Center
    ) {

        // Flecha "←" dibujada con tres líneas
        Canvas(
            modifier = Modifier.size(20.dp)
        ) {

            val grosor = 2.4.dp.toPx()
            val centroY = size.height / 2
            val izquierda = size.width * 0.15f
            val derecha = size.width * 0.85f
            val punta = size.width * 0.32f

            // Línea horizontal
            drawLine(Color.White, Offset(derecha, centroY), Offset(izquierda, centroY), grosor, StrokeCap.Round)

            // Las dos líneas de la punta
            drawLine(Color.White, Offset(izquierda, centroY), Offset(izquierda + punta, centroY - punta), grosor, StrokeCap.Round)
            drawLine(Color.White, Offset(izquierda, centroY), Offset(izquierda + punta, centroY + punta), grosor, StrokeCap.Round)
        }
    }
}