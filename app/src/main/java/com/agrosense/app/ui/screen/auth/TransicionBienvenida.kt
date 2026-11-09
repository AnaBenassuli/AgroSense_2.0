package com.agrosense.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroSuccess
import kotlinx.coroutines.delay

/**
 * Transición de bienvenida que se muestra encima del Home justo después del login:
 * un degradado verde en movimiento con un mensaje de bienvenida (unos 2 segundos).
 * Al terminar se desvanece y deja ver el Home.
 */
@Composable
fun TransicionBienvenida(
    onTerminado: () -> Unit
) {

    val aparicion = remember { Animatable(0f) }
    val salida = remember { Animatable(1f) }

    // Movimiento continuo del degradado (va y regresa)
    val transicion = rememberInfiniteTransition(label = "gradiente")

    val desplazamiento by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "desplazamiento"
    )

    LaunchedEffect(Unit) {
        aparicion.animateTo(1f, tween(durationMillis = 500))
        delay(1200)
        salida.animateTo(0f, tween(durationMillis = 500))
        onTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = salida.value }
            // Bloquea toques al Home mientras dura la animación
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .drawBehind {
                val ancho = size.width
                val alto = size.height
                val corrimiento = desplazamiento * ancho

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(AgroGreenDark, AgroSuccess, AgroGreen),
                        start = Offset(-ancho + corrimiento, 0f),
                        end = Offset(corrimiento + ancho * 0.5f, alto),
                        tileMode = TileMode.Mirror
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = aparicion.value
                translationY = (1f - aparicion.value) * 24.dp.toPx()
            }
        ) {

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_leaf),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¡Bienvenido!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Preparando tus cultivos...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}