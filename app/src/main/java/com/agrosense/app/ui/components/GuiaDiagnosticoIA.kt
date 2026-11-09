package com.agrosense.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import com.agrosense.app.ml.GuiaDiagnostico
import com.agrosense.app.ml.NivelConfianza
import com.agrosense.app.ml.NombresClases
import com.agrosense.app.ml.porcentaje

/**
 * Pasos para tomar una buena foto en el [modulo] elegido, y qué reconoce su modelo. Se puede
 * plegar para que el formulario no quede tan largo una vez que ya hay foto.
 */
@Composable
fun GuiaFoto(
    modulo: Modulo,
    expandida: Boolean,
    onAlternar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cómo tomar una buena foto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                TextButton(onClick = onAlternar) {
                    Text(if (expandida) "Ocultar" else "Ver pasos")
                }
            }

            if (expandida) {
                GuiaDiagnostico.pasos(modulo).forEachIndexed { i, paso ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text(
                            text = "${i + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(text = paso, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Text(
                    text = GuiaDiagnostico.queReconoce(modulo),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                )
            }
        }
    }
}

/** Aviso de que la IA puede equivocarse. Va junto al botón de analizar, siempre visible. */
@Composable
fun AvisoIA(modulo: Modulo, modifier: Modifier = Modifier) {
    Text(
        text = "ⓘ " + GuiaDiagnostico.avisoIA(modulo),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 6.dp)
    )
}

/**
 * Resultado de la IA explicado: qué sugiere, qué tan segura está, qué hacer y qué otras opciones
 * consideró. El color de la tarjeta sigue al nivel de confianza.
 */
@Composable
fun LecturaIA(
    modulo: Modulo,
    top3: List<Prediccion>,
    modifier: Modifier = Modifier
) {
    val mejor = top3.firstOrNull() ?: return
    val lectura = GuiaDiagnostico.leer(modulo, mejor)
    val esquema = MaterialTheme.colorScheme

    // Semáforo fijo y no colores del tema: en el tema de la app el "tertiary" sale rosa y una
    // confianza media parecía un error
    val (fondo, texto) = when (lectura.nivel) {
        NivelConfianza.ALTA -> Color(0xFFDDF0D5) to Color(0xFF1E4620)
        NivelConfianza.MEDIA -> Color(0xFFFFF0C2) to Color(0xFF5A4300)
        NivelConfianza.BAJA -> esquema.errorContainer to esquema.onErrorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = texto)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "La IA sugiere: ${NombresClases.legible(mejor.clase)} (${mejor.porcentaje()})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${lectura.nivel.titulo}. ${lectura.nivel.explicacion}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = lectura.consejo,
                style = MaterialTheme.typography.bodySmall
            )

            val otras = top3.drop(1)
            if (otras.isNotEmpty()) {
                Text(
                    text = "Otras opciones: " + otras.joinToString(" · ") {
                        "${NombresClases.legible(it.clase)} ${it.porcentaje()}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }

            Text(
                text = "Revisa el campo Resultado: cámbialo si no coincide con lo que ves.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
