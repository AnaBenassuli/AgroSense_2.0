package com.agrosense.app.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.ui.theme.AgroBorder
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroGreenPressed
import com.agrosense.app.ui.theme.AgroGreenSoft
import com.agrosense.app.ui.theme.AgroSuccess
import com.agrosense.app.ui.theme.AgroSurface
import com.agrosense.app.ui.theme.AgroSurfaceAlt
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary

@Composable
fun HomeScreen(
    nombreUsuario: String,
    totalParcelas: Int,
    totalDiagnosticos: Int,
    totalActividades: Int,
    onCerrarSesion: () -> Unit,
    onParcelasClick: () -> Unit,
    onDiagnosticoClick: () -> Unit,
    onHistorialClick: () -> Unit,
    onActividadesClick: () -> Unit,
    onPerfilClick: () -> Unit
) {

    // Animación de degradado al entrar después del login.
    // rememberSaveable evita que se repita al regresar de otra pantalla.
    var mostrarBienvenida by rememberSaveable {
        mutableStateOf(true)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
        ) {

            Spacer(
                modifier = Modifier.height(34.dp)
            )

            // HEADER

            Text(
                // El nombre ayuda a confirmar con qué cuenta se entró
                text = if (nombreUsuario.isBlank()) "Hola 👋" else "Hola, $nombreUsuario 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "¿Qué quieres revisar hoy?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // GUÍA PARA EMPEZAR: en las pruebas, un usuario nuevo no sabía por dónde empezar ni que el
            // diagnóstico necesita una parcela. Se oculta cuando ya completó los tres primeros pasos.
            if (totalParcelas == 0 || totalDiagnosticos == 0 || totalActividades == 0) {

                GuiaInicio(
                    tieneParcelas = totalParcelas > 0,
                    tieneDiagnosticos = totalDiagnosticos > 0,
                    tieneActividades = totalActividades > 0,
                    onParcelasClick = onParcelasClick,
                    onDiagnosticoClick = onDiagnosticoClick,
                    onActividadesClick = onActividadesClick,
                    onHistorialClick = onHistorialClick
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }

            // CTA PRINCIPAL

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    // Icono de cámara en un cuadro verde
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(AgroGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    Text(
                        text = "Diagnóstico rápido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Toma una foto de una hoja o de tu ganado y la app sugiere qué enfermedad podría tener.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Button(
                        onClick = onDiagnosticoClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {

                        Text(
                            text = "Nuevo diagnóstico",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // RESUMEN

            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SummaryCard(
                    title = if (totalParcelas == 1) "Parcela" else "Parcelas",
                    value = totalParcelas.toString(),
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = if (totalDiagnosticos == 1) "Diagnóstico" else "Diagnósticos",
                    value = totalDiagnosticos.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // HERRAMIENTAS

            Text(
                text = "Herramientas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            ToolCard(
                icon = R.drawable.ic_location,
                title = "Mis parcelas",
                description = "Consulta y administra tus cultivos.",
                onClick = onParcelasClick
            )

            ToolCard(
                icon = R.drawable.ic_history,
                title = "Historial",
                description = "Revisa diagnósticos anteriores.",
                onClick = onHistorialClick
            )

            ToolCard(
                icon = R.drawable.ic_event,
                title = "Actividades",
                description = "Registra riegos, tratamientos y labores.",
                onClick = onActividadesClick
            )

            ToolCard(
                icon = R.drawable.ic_person,
                title = "Mi perfil",
                description = "Consulta y edita los datos de tu cuenta.",
                onClick = onPerfilClick
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cerrar sesión")
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
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
private fun GuiaInicio(
    tieneParcelas: Boolean,
    tieneDiagnosticos: Boolean,
    tieneActividades: Boolean,
    onParcelasClick: () -> Unit,
    onDiagnosticoClick: () -> Unit,
    onActividadesClick: () -> Unit,
    onHistorialClick: () -> Unit
) {

    // Los tres pasos que cuentan para ocultar la guía (el historial es opcional)
    val completados = listOf(tieneParcelas, tieneDiagnosticos, tieneActividades).count { it }
    val faltan = 3 - completados

    // El paso "actual" es el primero sin hacer: es el único que se resalta
    val pasoActual =
        when {
            !tieneParcelas -> 1
            !tieneDiagnosticos -> 2
            !tieneActividades -> 3
            else -> 4
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AgroSurface)
            .border(1.dp, AgroBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {

        // ENCABEZADO: título + cuántos pasos faltan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = "¿Cómo empiezo?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AgroTextPrimary
                )

                Text(
                    text = "Sigue estos pasos para usar AgroSense",
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroTextSecondary
                )
            }

            Text(
                text = if (faltan == 1) "Falta 1" else "Faltan $faltan",
                style = MaterialTheme.typography.labelMedium,
                color = AgroGreenDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AgroGreenSoft)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BARRA DE PROGRESO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(AgroSurfaceAlt)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(completados / 3f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AgroGreen)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PASOS (línea de tiempo)
        PasoGuia(
            numero = 1,
            titulo = "Registra una parcela",
            descripcion = "El terreno o potrero que vas a revisar. Los diagnósticos se guardan dentro de una parcela.",
            hecho = tieneParcelas,
            actual = pasoActual == 1,
            textoBoton = "Ir a Mis parcelas",
            habilitado = true,
            esUltimo = false,
            onClick = onParcelasClick
        )

        PasoGuia(
            numero = 2,
            titulo = "Haz un diagnóstico",
            descripcion = "Elige Cultivo o Ganado, toma una foto de cerca y toca \"Analizar con IA\".",
            hecho = tieneDiagnosticos,
            actual = pasoActual == 2,
            textoBoton = "Nuevo diagnóstico",
            habilitado = tieneParcelas,
            esUltimo = false,
            onClick = onDiagnosticoClick
        )

        PasoGuia(
            numero = 3,
            titulo = "Registra riegos y tratamientos",
            descripcion = "Anota lo que haces en cada parcela: riegos, fertilizaciones, " +
                    "fumigaciones, vacunas del ganado o la cosecha.",
            hecho = tieneActividades,
            actual = pasoActual == 3,
            textoBoton = "Ir a Actividades",
            habilitado = tieneParcelas,
            esUltimo = false,
            onClick = onActividadesClick
        )

        PasoGuia(
            numero = 4,
            titulo = "Consulta el historial",
            descripcion = "Revisa los diagnósticos que guardaste en cada parcela.",
            hecho = false,
            actual = pasoActual == 4,
            textoBoton = "Ver historial",
            habilitado = tieneDiagnosticos,
            esUltimo = true,
            onClick = onHistorialClick
        )
    }
}

/**
 * Un paso de la guía con estilo de línea de tiempo:
 * círculo con número (o palomita si ya está hecho) y una línea que lo une con el siguiente.
 */
@Composable
private fun PasoGuia(
    numero: Int,
    titulo: String,
    descripcion: String,
    hecho: Boolean,
    actual: Boolean,
    textoBoton: String,
    habilitado: Boolean,
    esUltimo: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        // COLUMNA IZQUIERDA: círculo + línea
        Column(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            hecho -> AgroGreen
                            actual -> AgroGreenSoft
                            else -> AgroSurfaceAlt
                        }
                    )
                    .then(
                        if (actual && !hecho) {
                            Modifier.border(1.5.dp, AgroGreen, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hecho) "✓" else numero.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        when {
                            hecho -> Color.White
                            actual -> AgroGreenDark
                            else -> AgroTextTertiary
                        }
                )
            }

            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (hecho) AgroGreen else AgroBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // COLUMNA DERECHA: texto y botón
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 3.dp, bottom = if (esUltimo) 0.dp else 18.dp)
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color =
                    when {
                        hecho -> AgroSuccess
                        actual -> AgroTextPrimary
                        else -> AgroTextSecondary
                    }
            )

            // Los pasos hechos solo muestran el título, para que la guía no se vea tan larga
            if (!hecho) {

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Botón tipo pastilla: verde lleno en el paso actual, tenue en los demás
                Text(
                    text = "$textoBoton  →",
                    style = MaterialTheme.typography.labelLarge,
                    color =
                        when {
                            !habilitado -> AgroTextTertiary
                            actual -> Color.White
                            else -> AgroGreenDark
                        },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                !habilitado -> AgroSurfaceAlt
                                actual -> AgroGreenPressed
                                else -> AgroGreenSoft
                            }
                        )
                        .clickable(enabled = habilitado, onClick = onClick)
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )

                if (!habilitado) {

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Disponible cuando completes el paso anterior.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroTextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Tarjeta de herramienta: icono a la izquierda, texto al centro y flechita a la derecha. */
@Composable
private fun ToolCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icono sobre un cuadro verde tenue
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(AgroGreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = AgroGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = AgroTextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}