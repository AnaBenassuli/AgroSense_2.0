package com.agrosense.app.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroIconBadge
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroStatCard
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.theme.AgroBackground
import com.agrosense.app.ui.theme.AgroBorder
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroSurface
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
import com.agrosense.app.ui.components.AgroSenseLogo

@Composable
fun HomeScreen(
    onParcelasClick: () -> Unit,
    onDiagnosticoClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onActividadesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {

    val scroll = rememberScrollState()

    // Animación de degradado al entrar después del login.
    // rememberSaveable evita que se repita al regresar de otra pantalla.
    var mostrarBienvenida by rememberSaveable {
        mutableStateOf(true)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Toda la pantalla se desplaza; la barrita de la derecha indica cuánto falta por ver
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AgroBackground)
                .statusBarsPadding()
                .barraDeScroll(scroll)
                .verticalScroll(scroll)
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {

            // BARRA SUPERIOR: marca + acceso al perfil
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AgroSenseLogo(
                    height = 60.dp
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AgroSurface)
                        .border(1.dp, AgroBorder, CircleShape)
                        .clickable(onClick = onPerfilClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = "Perfil y usuarios",
                        tint = AgroTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // SALUDO
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hola 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = AgroTextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "¿Qué quieres revisar hoy?",
                style = MaterialTheme.typography.headlineMedium,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CTA PRINCIPAL (tarjeta grafito-verde, la de mayor jerarquía)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AgroGreenDark)
                    .padding(20.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Diagnóstico rápido",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Toma una foto de tu cultivo y registra un nuevo diagnóstico.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                AgroPrimaryButton(
                    text = "Nuevo diagnóstico",
                    icon = R.drawable.ic_add,
                    onClick = onDiagnosticoClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // RESUMEN
            Spacer(modifier = Modifier.height(28.dp))

            AgroSectionHeader(title = "Resumen")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                AgroStatCard(
                    label = "Parcelas",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )

                AgroStatCard(
                    label = "Diagnósticos",
                    value = "0",
                    tone = AgroTone.SUCCESS,
                    modifier = Modifier.weight(1f)
                )
            }

            // HERRAMIENTAS
            Spacer(modifier = Modifier.height(28.dp))

            AgroSectionHeader(title = "Herramientas")

            Spacer(modifier = Modifier.height(12.dp))

            AgroCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {

                ToolRow(
                    icon = R.drawable.ic_location,
                    title = "Mis parcelas",
                    description = "Consulta y administra tus cultivos.",
                    onClick = onParcelasClick
                )

                ToolDivider()

                ToolRow(
                    icon = R.drawable.ic_history,
                    title = "Historial",
                    description = "Revisa diagnósticos anteriores.",
                    onClick = onHistorialClick
                )

                ToolDivider()

                ToolRow(
                    icon = R.drawable.ic_event,
                    title = "Actividades",
                    description = "Registra riegos, tratamientos y labores.",
                    onClick = onActividadesClick
                )

                ToolDivider()

                ToolRow(
                    icon = R.drawable.ic_group,
                    title = "Perfil y usuarios",
                    description = "Administra los usuarios registrados en AgroSense.",
                    onClick = onPerfilClick
                )
            }

            // Espacio para que lo último no quede debajo de la barra de gestos del teléfono
            Spacer(modifier = Modifier.navigationBarsPadding())
        }

        // DEGRADADO DE BIENVENIDA (encima del Home, solo al entrar)
        if (mostrarBienvenida) {
            TransicionBienvenida(
                onTerminado = { mostrarBienvenida = false }
            )
        }
    }
}

@Composable
private fun ToolRow(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AgroIconBadge(
            icon = icon,
            size = 42.dp,
            iconSize = 20.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AgroTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = AgroTextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToolDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        thickness = 1.dp,
        color = AgroBorder
    )
}

/**
 * Dibuja una barra delgada a la derecha que muestra la posición del scroll.
 * Solo aparece cuando el contenido es más alto que la pantalla.
 */
private fun Modifier.barraDeScroll(estado: ScrollState): Modifier =
    drawWithContent {
        drawContent()

        val maximo = estado.maxValue
        if (maximo <= 0 || maximo == Int.MAX_VALUE) return@drawWithContent

        val alturaVista = size.height
        val alturaContenido = alturaVista + maximo
        val alturaBarra = (alturaVista * alturaVista / alturaContenido)
            .coerceAtLeast(40.dp.toPx())
        val posicion = estado.value.toFloat() / maximo * (alturaVista - alturaBarra)

        drawRoundRect(
            color = AgroTextTertiary.copy(alpha = 0.6f),
            topLeft = Offset(size.width - 6.dp.toPx(), posicion),
            size = Size(4.dp.toPx(), alturaBarra),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }