package com.agrosense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Mensajes cortos de confirmación ("Parcela guardada", "Actividad eliminada"...).
 * Se pueden mandar desde cualquier pantalla con: Avisos.mostrar("texto")
 */
object Avisos {

    private val mensajes = MutableSharedFlow<String>(extraBufferCapacity = 4)

    val flujo = mensajes.asSharedFlow()

    fun mostrar(texto: String) {
        mensajes.tryEmit(texto)
    }
}

/**
 * Muestra los avisos abajo de la pantalla. Se coloca una sola vez, en MainActivity.
 * Si llega un aviso nuevo mientras se ve otro, el nuevo reemplaza al anterior.
 */
@Composable
fun AvisoHost(
    modifier: Modifier = Modifier
) {

    val estado = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Avisos.flujo.collectLatest { texto ->
            estado.showSnackbar(texto)
        }
    }

    SnackbarHost(
        hostState = estado,
        modifier = modifier
    ) { datos ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(AgroGreenDark)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Palomita en un círculo verde
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AgroGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = datos.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}