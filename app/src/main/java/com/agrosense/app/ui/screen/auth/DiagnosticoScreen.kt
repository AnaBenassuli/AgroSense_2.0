package com.agrosense.app.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Prediccion
import com.agrosense.app.ml.ImageClassifier
import com.agrosense.app.ml.porcentaje
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun DiagnosticoScreen(
    diagnosticoViewModel: DiagnosticoViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val parcelas by parcelaViewModel.parcelas.collectAsState()

    var parcelaSeleccionadaId by remember {
        mutableStateOf<Long?>(null)
    }

    if (
        parcelaSeleccionadaId == null &&
        parcelas.isNotEmpty()
    ) {
        parcelaSeleccionadaId = parcelas.first().id
    }

    val diagnosticos =
        parcelaSeleccionadaId?.let { id ->

            diagnosticoViewModel
                .observarPorParcela(id)
                .collectAsState(
                    initial = emptyList()
                ).value

        } ?: emptyList()

    var mostrarMenuParcelas by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var diagnosticoEditando by remember {
        mutableStateOf<Diagnostico?>(null)
    }

    var diagnosticoAEliminar by remember {
        mutableStateOf<Diagnostico?>(null)
    }

    var resultado by remember {
        mutableStateOf("")
    }

    var confianza by remember {
        mutableStateOf("")
    }

    var observaciones by remember {
        mutableStateOf("")
    }

    var imagenBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var imagenRuta by remember {
        mutableStateOf("")
    }

    // Clasificador en el dispositivo. Si el modelo no está en assets/,
    // `disponible` queda en false y el botón de análisis se deshabilita:
    // el formulario sigue funcionando a mano.
    val clasificador = remember(context) {
        ImageClassifier(context)
    }

    DisposableEffect(clasificador) {
        onDispose { clasificador.close() }
    }

    var top3Detectado by remember {
        mutableStateOf<List<Prediccion>>(emptyList())
    }

    var mensajeAnalisis by remember {
        mutableStateOf<String?>(null)
    }

    val camaraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            bitmap?.let {

                imagenBitmap = it

                imagenRuta =
                    guardarBitmapEnInterno(
                        context = context,
                        bitmap = it
                    )
            }
        }

    val galeriaLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                val bitmap =
                    uriABitmap(
                        context = context,
                        uri = it
                    )

                bitmap?.let { foto ->

                    imagenBitmap = foto

                    imagenRuta =
                        guardarBitmapEnInterno(
                            context = context,
                            bitmap = foto
                        )
                }
            }
        }

    fun limpiarFormulario() {
        resultado = ""
        confianza = ""
        observaciones = ""
        imagenBitmap = null
        imagenRuta = ""
        diagnosticoEditando = null
        top3Detectado = emptyList()
        mensajeAnalisis = null
    }

    /**
     * Rellena resultado, confianza y top-3 a partir de la foto, usando el
     * modelo de assets/. No guarda nada: deja los campos editables para que
     * el usuario corrija antes de guardar, que es lo prudente — en fotos de
     * campo el modelo acierta cerca de un 25% de las veces.
     */
    fun analizarConIA() {
        val foto = imagenBitmap

        if (foto == null) {
            mensajeAnalisis = "Toma o elige una foto primero."
            return
        }

        val analisis = clasificador.classify(foto)

        if (!analisis.esValido) {
            mensajeAnalisis = analisis.label
            top3Detectado = emptyList()
            return
        }

        resultado = analisis.label

        confianza = (analisis.confidence * 100f)
            .toInt()
            .toString()

        top3Detectado = analisis.top3

        mensajeAnalisis = analisis.top3
            .joinToString(" · ") { p ->
                "${p.clase} ${p.porcentaje()}"
            }
    }

    fun abrirNuevoDiagnostico() {
        limpiarFormulario()
        mostrarFormulario = true
    }

    fun abrirEditarDiagnostico(
        diagnostico: Diagnostico
    ) {

        diagnosticoEditando =
            diagnostico

        resultado =
            diagnostico.resultado

        confianza =
            (diagnostico.confianza * 100f)
                .toInt()
                .toString()

        observaciones =
            diagnostico.observaciones ?: ""

        imagenRuta =
            diagnostico.imagenRuta

        imagenBitmap =
            BitmapFactory.decodeFile(
                diagnostico.imagenRuta
            )

        // Conserva el top-3 que se guardó al crear el diagnóstico, para no
        // vaciarlo al actualizar desde el formulario.
        top3Detectado = diagnostico.top3

        mensajeAnalisis = null

        mostrarFormulario = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            )
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Text(
                text = "Diagnósticos",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Registra y consulta los diagnósticos de tus cultivos.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "Parcela",
            style =
                MaterialTheme.typography.labelMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Column {

            OutlinedButton(
                onClick = {
                    mostrarMenuParcelas = true
                },
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(14.dp)
            ) {

                val nombre =
                    parcelas.firstOrNull {
                        it.id ==
                                parcelaSeleccionadaId
                    }?.nombre
                        ?: "Seleccionar parcela"

                Text(nombre)
            }

            DropdownMenu(
                expanded =
                    mostrarMenuParcelas,
                onDismissRequest = {
                    mostrarMenuParcelas = false
                }
            ) {

                parcelas.forEach { parcela ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                "${parcela.nombre} · ${parcela.cultivo}"
                            )
                        },
                        onClick = {

                            parcelaSeleccionadaId =
                                parcela.id

                            mostrarMenuParcelas =
                                false
                        }
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Button(
            onClick = {
                abrirNuevoDiagnostico()
            },
            enabled =
                parcelaSeleccionadaId != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape =
                RoundedCornerShape(14.dp)
        ) {

            Text("+ Nuevo diagnóstico")
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        if (parcelas.isEmpty()) {

            DiagnosticoEmptyCard(
                titulo =
                    "Primero agrega una parcela",
                descripcion =
                    "Necesitas una parcela para registrar diagnósticos."
            )

        } else if (
            diagnosticos.isEmpty()
        ) {

            DiagnosticoEmptyCard(
                titulo =
                    "Sin diagnósticos",
                descripcion =
                    "Todavía no hay diagnósticos registrados para esta parcela."
            )

        } else {

            Text(
                text =
                    if (diagnosticos.size == 1) {
                        "1 diagnóstico registrado"
                    } else {
                        "${diagnosticos.size} diagnósticos registrados"
                    },
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items =
                        diagnosticos,
                    key = {
                        it.id
                    }
                ) { diagnostico ->

                    DiagnosticoCard(
                        diagnostico =
                            diagnostico,
                        onEditClick = {
                            abrirEditarDiagnostico(
                                diagnostico
                            )
                        },
                        onDeleteClick = {
                            diagnosticoAEliminar =
                                diagnostico
                        }
                    )
                }
            }
        }
    }

    // ==================================================
    // CREATE / UPDATE
    // ==================================================

    if (mostrarFormulario) {

        AlertDialog(
            onDismissRequest = {
                mostrarFormulario =
                    false
                limpiarFormulario()
            },

            shape =
                RoundedCornerShape(18.dp),

            title = {

                Text(
                    text =
                        if (
                            diagnosticoEditando ==
                            null
                        ) {
                            "Nuevo diagnóstico"
                        } else {
                            "Editar diagnóstico"
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                LazyColumn {

                    item {

                        imagenBitmap?.let {

                            Image(
                                bitmap =
                                    it.asImageBitmap(),
                                contentDescription =
                                    "Imagen del diagnóstico",
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                        ),
                                contentScale =
                                    ContentScale.Crop
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        12.dp
                                    )
                            )
                        }

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            OutlinedButton(
                                onClick = {
                                    camaraLauncher
                                        .launch(null)
                                },
                                modifier =
                                    Modifier.weight(1f)
                            ) {
                                Text("Cámara")
                            }

                            OutlinedButton(
                                onClick = {
                                    galeriaLauncher
                                        .launch(
                                            "image/*"
                                        )
                                },
                                modifier =
                                    Modifier.weight(1f)
                            ) {
                                Text("Galería")
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Button(
                            onClick = {
                                analizarConIA()
                            },
                            enabled =
                                clasificador.disponible &&
                                    imagenBitmap != null,
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {
                            Text("Analizar con IA")
                        }

                        if (
                            !clasificador.disponible
                        ) {
                            Text(
                                text =
                                    "El modelo no está " +
                                        "instalado en assets/. " +
                                        "Puedes llenar el " +
                                        "diagnóstico a mano.",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }

                        mensajeAnalisis?.let { texto ->

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text = texto,
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )

                            Text(
                                text =
                                    "Sugerencia del modelo: " +
                                        "revísala antes de guardar.",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value =
                                resultado,
                            onValueChange = {
                                resultado = it
                            },
                            label = {
                                Text(
                                    "Resultado"
                                )
                            },
                            placeholder = {
                                Text(
                                    "Ej. Mancha foliar"
                                )
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value =
                                confianza,
                            onValueChange = {
                                confianza = it
                            },
                            label = {
                                Text(
                                    "Confianza (%)"
                                )
                            },
                            placeholder = {
                                Text("Ej. 92")
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        OutlinedTextField(
                            value =
                                observaciones,
                            onValueChange = {
                                observaciones = it
                            },
                            label = {
                                Text(
                                    "Observaciones"
                                )
                            },
                            minLines = 3,
                            maxLines = 5,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val parcelaId =
                            parcelaSeleccionadaId

                        val porcentaje =
                            confianza
                                .toFloatOrNull()

                        if (
                            parcelaId != null &&
                            resultado.isNotBlank() &&
                            imagenRuta.isNotBlank() &&
                            porcentaje != null &&
                            porcentaje in 0f..100f
                        ) {

                            val actual =
                                diagnosticoEditando

                            if (actual == null) {

                                diagnosticoViewModel
                                    .registrarDiagnostico(
                                        Diagnostico(
                                            parcelaId =
                                                parcelaId,
                                            imagenRuta =
                                                imagenRuta,
                                            resultado =
                                                resultado.trim(),
                                            confianza =
                                                porcentaje /
                                                        100f,
                                            top3 =
                                                top3Detectado,
                                            observaciones =
                                                observaciones
                                                    .trim()
                                                    .ifBlank {
                                                        null
                                                    }
                                        )
                                    )

                            } else {

                                diagnosticoViewModel
                                    .actualizarDiagnostico(
                                        actual.copy(
                                            parcelaId =
                                                parcelaId,
                                            imagenRuta =
                                                imagenRuta,
                                            resultado =
                                                resultado.trim(),
                                            confianza =
                                                porcentaje /
                                                        100f,
                                            top3 =
                                                top3Detectado,
                                            observaciones =
                                                observaciones
                                                    .trim()
                                                    .ifBlank {
                                                        null
                                                    }
                                        )
                                    )
                            }

                            mostrarFormulario =
                                false

                            limpiarFormulario()
                        }
                    }
                ) {

                    Text(
                        if (
                            diagnosticoEditando ==
                            null
                        ) {
                            "Guardar"
                        } else {
                            "Actualizar"
                        }
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        mostrarFormulario =
                            false

                        limpiarFormulario()
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }

    // ==================================================
    // DELETE
    // ==================================================

    diagnosticoAEliminar?.let {
            diagnostico ->

        AlertDialog(
            onDismissRequest = {
                diagnosticoAEliminar =
                    null
            },

            shape =
                RoundedCornerShape(18.dp),

            title = {

                Text(
                    text =
                        "Eliminar diagnóstico",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            "¿Deseas eliminar el diagnóstico \"${diagnostico.resultado}\"?"
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Esta acción no se puede deshacer.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        diagnosticoViewModel
                            .eliminarDiagnostico(
                                diagnostico
                            )

                        diagnosticoAEliminar =
                            null
                    }
                ) {

                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        diagnosticoAEliminar =
                            null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DiagnosticoCard(
    diagnostico: Diagnostico,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Text(
                text =
                    diagnostico.resultado,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "Confianza: ${
                        (diagnostico.confianza * 100)
                            .toInt()
                    }%",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.primary
            )

            diagnostico.observaciones
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text = it,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                OutlinedButton(
                    onClick =
                        onEditClick
                ) {

                    Text("Editar")
                }

                Spacer(
                    modifier =
                        Modifier.padding(
                            horizontal = 4.dp
                        )
                )

                TextButton(
                    onClick =
                        onDeleteClick
                ) {

                    Text(
                        text = "Eliminar",
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticoEmptyCard(
    titulo: String,
    descripcion: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(
                text = titulo,
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text = descripcion,
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun uriABitmap(
    context: Context,
    uri: Uri
): Bitmap? {

    return try {

        context
            .contentResolver
            .openInputStream(uri)
            ?.use {
                BitmapFactory.decodeStream(it)
            }

    } catch (
        exception: Exception
    ) {
        null
    }
}

private fun guardarBitmapEnInterno(
    context: Context,
    bitmap: Bitmap
): String {

    val directorio =
        File(
            context.filesDir,
            "diagnosticos"
        )

    if (!directorio.exists()) {
        directorio.mkdirs()
    }

    val archivo =
        File(
            directorio,
            "diagnostico_${System.currentTimeMillis()}.jpg"
        )

    FileOutputStream(archivo)
        .use { output ->

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                output
            )
        }

    return archivo.absolutePath
}