package com.agrosense.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.agrosense.app.R

/**
 * Logo de AgroSense sin el recuadro blanco de la imagen.
 *
 * El PNG trae fondo blanco. Se dibuja con modo "Multiply": el blanco toma el color
 * del fondo que tenga detrás (el papel de la app) y los verdes quedan igual,
 * así que el logo se ve sin fondo sin tener que editar la imagen.
 *
 * @param height altura deseada; si no cabe a lo ancho, se reduce sola.
 */
@Composable
fun AgroSenseLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    height: Dp = if (compact) 28.dp else 132.dp
) {
    val logo = ImageBitmap.imageResource(R.drawable.agrosense_logo)
    val proporcion = logo.width.toFloat() / logo.height.toFloat()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val alturaFinal = minOf(
            height,
            maxWidth / proporcion
        )

        Canvas(
            modifier = Modifier
                .height(alturaFinal)
                .aspectRatio(proporcion)
                .semantics { contentDescription = "Logo de AgroSense" }
        ) {
            drawImage(
                image = logo,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                blendMode = BlendMode.Multiply,
                filterQuality = FilterQuality.High
            )
        }
    }
}