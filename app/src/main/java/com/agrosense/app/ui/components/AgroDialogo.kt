package com.agrosense.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.theme.AgroGreenDark

/**
 * Ventana emergente con el estilo de AgroSense. Se usa igual que AlertDialog:
 * - Fondo blanco (sin el tono morado que Material 3 pone por defecto).
 * - Textos y campos del contenido en verde oscuro.
 * - El título y los botones conservan sus colores normales.
 */
@Composable
fun AgroDialogo(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp)
) {

    // Dentro del contenido, los colores "de texto" del tema pasan a verde oscuro:
    // así también cambian las etiquetas y el texto de los campos (OutlinedTextField)
    val coloresContenido = MaterialTheme.colorScheme.copy(
        onSurface = AgroGreenDark,
        onSurfaceVariant = AgroGreenDark
    )

    val textoVerde: (@Composable () -> Unit)? =
        text?.let { contenido ->
            @Composable {
                MaterialTheme(colorScheme = coloresContenido) {
                    contenido()
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = textoVerde,
        shape = shape,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        textContentColor = AgroGreenDark
    )
}