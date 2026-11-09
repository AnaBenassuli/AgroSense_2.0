package com.agrosense.app.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.AgroSenseLogo
import com.agrosense.app.ui.theme.AgroBackground
import com.agrosense.app.ui.theme.AgroBorder
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida animada que aparece antes del login (unos 2 segundos):
 * 1. El logo aparece y "rebota" un poco al crecer.
 * 2. Aparece el lema y se llena una barrita verde.
 * 3. Todo se desvanece y se llama a [onTerminado].
 */
@Composable
fun SplashScreen(
    onTerminado: () -> Unit
) {

    val escalaLogo = remember { Animatable(0.6f) }
    val aparicionLogo = remember { Animatable(0f) }
    val aparicionTexto = remember { Animatable(0f) }
    val progreso = remember { Animatable(0f) }
    val salida = remember { Animatable(1f) }

    LaunchedEffect(Unit) {

        // 1. Logo: aparece y crece con un pequeño rebote
        launch {
            aparicionLogo.animateTo(1f, tween(durationMillis = 500))
        }

        escalaLogo.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        )

        // 2. Lema y barra de carga
        launch {
            aparicionTexto.animateTo(1f, tween(durationMillis = 400))
        }

        progreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )

        delay(200)

        // 3. Salida suave hacia el login
        salida.animateTo(0f, tween(durationMillis = 350))

        onTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = salida.value }
            .background(AgroBackground),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(contentAlignment = Alignment.Center) {

                AgroSenseLogo(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .graphicsLayer {
                            scaleX = escalaLogo.value
                            scaleY = escalaLogo.value
                        }
                )

                // Velo del color del fondo que se va quitando: hace que el logo
                // aparezca poco a poco sin perder el efecto "sin fondo"
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(AgroBackground.copy(alpha = 1f - aparicionLogo.value))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cultiva · Analiza · Decide",
                style = MaterialTheme.typography.labelMedium,
                color = AgroTextSecondary,
                modifier = Modifier.graphicsLayer { alpha = aparicionTexto.value }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Barra de carga
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AgroBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso.value)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AgroGreen)
                )
            }
        }
    }
}