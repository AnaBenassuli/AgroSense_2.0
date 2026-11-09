package com.agrosense.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            title = "Mis parcelas",
            description = "Consulta y administra tus cultivos.",
            onClick = onParcelasClick
        )

        ToolCard(
            title = "Historial",
            description = "Revisa diagnósticos anteriores.",
            onClick = onHistorialClick
        )

        ToolCard(
            title = "Actividades",
            description = "Registra riegos, tratamientos y labores.",
            onClick = onActividadesClick
        )

        // NUEVA HERRAMIENTA
        ToolCard(
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "¿Cómo empiezo?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            PasoGuia(
                numero = 1,
                titulo = "Registra una parcela",
                descripcion = "El terreno o potrero que vas a revisar. Los diagnósticos se guardan dentro de una parcela.",
                hecho = tieneParcelas,
                textoBoton = "Ir a Mis parcelas",
                habilitado = true,
                onClick = onParcelasClick
            )

            PasoGuia(
                numero = 2,
                titulo = "Haz un diagnóstico",
                descripcion = "Elige Cultivo o Ganado, toma una foto de cerca y toca \"Analizar con IA\".",
                hecho = tieneDiagnosticos,
                textoBoton = "Nuevo diagnóstico",
                habilitado = tieneParcelas,
                onClick = onDiagnosticoClick
            )

            PasoGuia(
                numero = 3,
                titulo = "Registra riegos y tratamientos",
                descripcion = "Anota lo que haces en cada parcela: riegos, fertilizaciones, " +
                    "fumigaciones, vacunas del ganado o la cosecha. Así llevas el control de tus labores.",
                hecho = tieneActividades,
                textoBoton = "Ir a Actividades",
                habilitado = tieneParcelas,
                onClick = onActividadesClick
            )

            PasoGuia(
                numero = 4,
                titulo = "Consulta el historial",
                descripcion = "Revisa los diagnósticos que guardaste en cada parcela.",
                hecho = false,
                textoBoton = "Ver historial",
                habilitado = tieneDiagnosticos,
                onClick = onHistorialClick
            )
        }
    }
}

@Composable
private fun PasoGuia(
    numero: Int,
    titulo: String,
    descripcion: String,
    hecho: Boolean,
    textoBoton: String,
    habilitado: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {

        Text(
            text = if (hecho) "✓  $titulo" else "$numero.  $titulo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color =
                if (hecho) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
        )

        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        if (!hecho) {

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedButton(
                onClick = onClick,
                enabled = habilitado,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(textoBoton)
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

@Composable
private fun ToolCard(
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

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}