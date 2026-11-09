package com.agrosense.app.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agrosense.app.R
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import com.agrosense.app.ml.ImageClassifier
import com.agrosense.app.ml.NombresClases
import com.agrosense.app.ml.porcentaje
import com.agrosense.app.ui.components.AgroBanner
import com.agrosense.app.ui.components.AgroCard
import com.agrosense.app.ui.components.AgroCardActions
import com.agrosense.app.ui.components.AgroDialog
import com.agrosense.app.ui.components.AgroEmptyState
import com.agrosense.app.ui.components.AgroIconBadge
import com.agrosense.app.ui.components.AgroOverline
import com.agrosense.app.ui.components.AgroPrimaryButton
import com.agrosense.app.ui.components.AgroProgressBar
import com.agrosense.app.ui.components.AgroScreen
import com.agrosense.app.ui.components.AgroSecondaryButton
import com.agrosense.app.ui.components.AgroSectionHeader
import com.agrosense.app.ui.components.AgroSegmented
import com.agrosense.app.ui.components.AgroSelectField
import com.agrosense.app.ui.components.AgroStatusChip
import com.agrosense.app.ui.components.AgroTextField
import com.agrosense.app.ui.components.AgroTonalButton
import com.agrosense.app.ui.components.AgroTone
import com.agrosense.app.ui.components.AgroTopBar
import com.agrosense.app.ui.components.FieldSpacer
import com.agrosense.app.ui.theme.AgroBorder
import com.agrosense.app.ui.theme.AgroGreen
import com.agrosense.app.ui.theme.AgroGreenDark
import com.agrosense.app.ui.theme.AgroSurfaceAlt
import com.agrosense.app.ui.theme.AgroTextPrimary
import com.agrosense.app.ui.theme.AgroTextSecondary
import com.agrosense.app.ui.theme.AgroTextTertiary
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * @param modulo módulo con el que abre el formulario. Dentro del formulario se puede cambiar con
 *   el selector Cultivo | Ganado; la pantalla de selección de módulo (tarea 31) puede pasar
 *   GANADO aquí para que abra directo en ganado.
 */
@Composable
fun DiagnosticoScreen(
    diagnosticoViewModel: DiagnosticoViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit,
    modulo: Modulo = Modulo.CULTIVO
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
    var moduloActual by remember {
        mutableStateOf(modulo)
    }

    // Uno por módulo: al cambiar de módulo se crea el otro y el DisposableEffect cierra el anterior
    val clasificador = remember(context, moduloActual) {
        ImageClassifier(context, moduloActual)
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

        resultado = NombresClases.legible(analisis.label)

        confianza = (analisis.confidence * 100f)
            .toInt()
            .toString()

        top3Detectado = analisis.top3

        mensajeAnalisis = analisis.top3
            .joinToString(" · ") { p ->
                "${NombresClases.legible(p.clase)} ${p.porcentaje()}"
            }
    }

    /**
     * Cambia el modelo con el que se analiza. El resultado anterior se descarta porque venía del
     * otro modelo (etiquetas de cultivo en un diagnóstico de ganado, o al revés); la foto se queda.
     */
    fun cambiarModulo(nuevo: Modulo) {
        if (nuevo == moduloActual) return
        moduloActual = nuevo
        resultado = ""
        confianza = ""
        top3Detectado = emptyList()
        mensajeAnalisis = null
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

        // Se edita con el modelo con el que se hizo
        moduloActual =
            diagnostico.modulo

        // Los diagnósticos guardados antes de la traducción tienen la etiqueta en inglés
        resultado =
            NombresClases.legible(diagnostico.resultado)

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

    AgroScreen(
        bottomBar = {
            AgroPrimaryButton(
                text = "Nuevo diagnóstico",
                icon = R.drawable.ic_camera,
                onClick = { abrirNuevoDiagnostico() },
                enabled = parcelaSeleccionadaId != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {

        AgroTopBar(
            title = "Diagnósticos",
            onBack = onBackClick
        )

        Text(
            text = "Registra y consulta los diagnósticos de tus cultivos.",
            style = MaterialTheme.typography.bodyMedium,
            color = AgroTextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SELECTOR DE PARCELA
        AgroSelectField(
            label = "Parcela",
            valueText =
                parcelas.firstOrNull {
                    it.id == parcelaSeleccionadaId
                }?.let {
                    "${it.nombre} · ${it.cultivo}"
                } ?: "Seleccionar parcela",
            options = parcelas,
            optionLabel = { "${it.nombre} · ${it.cultivo}" },
            onSelect = { parcelaSeleccionadaId = it.id },
            leadingIcon = R.drawable.ic_location,
            enabled = parcelas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (parcelas.isEmpty()) {

            AgroEmptyState(
                icon = R.drawable.ic_location,
                title = "Primero agrega una parcela",
                description = "Necesitas una parcela para registrar diagnósticos."
            )

        } else if (
            diagnosticos.isEmpty()
        ) {

            AgroEmptyState(
                icon = R.drawable.ic_leaf,
                title = "Sin diagnósticos",
                description = "Todavía no hay diagnósticos registrados para esta parcela."
            )

        } else {

            AgroSectionHeader(
                title = "Diagnósticos de la parcela",
                trailing =
                    if (diagnosticos.size == 1) {
                        "1 registrado"
                    } else {
                        "${diagnosticos.size} registrados"
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                items(
                    items = diagnosticos,
                    key = {
                        it.id
                    }
                ) { diagnostico ->

                    DiagnosticoCard(
                        diagnostico = diagnostico,
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

        val porcentajeIngresado =
            confianza.toFloatOrNull()

        val confianzaValida =
            porcentajeIngresado != null &&
                    porcentajeIngresado in 0f..100f

        val formularioValido =
            parcelaSeleccionadaId != null &&
                    resultado.isNotBlank() &&
                    imagenRuta.isNotBlank() &&
                    confianzaValida

        AgroDialog(
            title =
                if (diagnosticoEditando == null) {
                    "Nuevo diagnóstico"
                } else {
                    "Editar diagnóstico"
                },
            confirmText =
                if (diagnosticoEditando == null) {
                    "Guardar"
                } else {
                    "Actualizar"
                },
            confirmEnabled = formularioValido,
            onDismiss = {
                mostrarFormulario = false
                limpiarFormulario()
            },
            onConfirm = {

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
                                    parcelaId = parcelaId,
                                    imagenRuta = imagenRuta,
                                    resultado = resultado.trim(),
                                    confianza = porcentaje / 100f,
                                    top3 = top3Detectado,
                                    observaciones =
                                        observaciones
                                            .trim()
                                            .ifBlank {
                                                null
                                            },
                                    modulo = moduloActual
                                )
                            )

                    } else {

                        diagnosticoViewModel
                            .actualizarDiagnostico(
                                actual.copy(
                                    parcelaId = parcelaId,
                                    imagenRuta = imagenRuta,
                                    resultado = resultado.trim(),
                                    confianza = porcentaje / 100f,
                                    top3 = top3Detectado,
                                    observaciones =
                                        observaciones
                                            .trim()
                                            .ifBlank {
                                                null
                                            },
                                    modulo = moduloActual
                                )
                            )
                    }

                    mostrarFormulario =
                        false

                    limpiarFormulario()
                }
            }
        ) {

            // MÓDULO: Cultivo | Ganado
            AgroSegmented(
                options = listOf(
                    Modulo.CULTIVO to "Cultivo",
                    Modulo.GANADO to "Ganado"
                ),
                selected = moduloActual,
                onSelect = { cambiarModulo(it) }
            )

            if (moduloActual == Modulo.GANADO) {

                Spacer(modifier = Modifier.height(10.dp))

                AgroBanner(
                    text =
                        "Toma la foto de cerca, al costado o lomo del animal. " +
                                "El resultado es orientativo: ante sospecha de " +
                                "dermatosis nodular, consulta a un veterinario.",
                    tone = AgroTone.WARNING
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FOTO
            val foto = imagenBitmap

            if (foto != null) {

                Image(
                    bitmap = foto.asImageBitmap(),
                    contentDescription = "Imagen del diagnóstico",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

            } else {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AgroSurfaceAlt)
                        .border(1.dp, AgroBorder, RoundedCornerShape(14.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    AgroIconBadge(
                        icon = R.drawable.ic_image,
                        tone = AgroTone.ACCENT
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text =
                            if (moduloActual == Modulo.GANADO) {
                                "Agrega una foto del animal"
                            } else {
                                "Agrega una foto del cultivo"
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgroTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                AgroSecondaryButton(
                    text = "Cámara",
                    icon = R.drawable.ic_camera,
                    onClick = {
                        camaraLauncher
                            .launch(null)
                    },
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )

                AgroSecondaryButton(
                    text = "Galería",
                    icon = R.drawable.ic_image,
                    onClick = {
                        galeriaLauncher
                            .launch(
                                "image/*"
                            )
                    },
                    height = 48.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ANÁLISIS CON IA
            AgroTonalButton(
                text = "Analizar con IA",
                icon = R.drawable.ic_sparkle,
                onClick = {
                    analizarConIA()
                },
                enabled =
                    clasificador.disponible &&
                            imagenBitmap != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (
                !clasificador.disponible
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                AgroBanner(
                    text =
                        "El modelo no está " +
                                "instalado en assets/. " +
                                "Puedes llenar el " +
                                "diagnóstico a mano.",
                    tone = AgroTone.WARNING
                )
            }

            mensajeAnalisis?.let { texto ->

                Spacer(modifier = Modifier.height(12.dp))

                if (top3Detectado.isNotEmpty()) {

                    ResultadosModelo(
                        predicciones = top3Detectado
                    )

                } else {

                    AgroBanner(
                        text = texto,
                        tone = AgroTone.INFO
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DATOS DEL DIAGNÓSTICO
            AgroTextField(
                value = resultado,
                onValueChange = { resultado = it },
                label = "Resultado",
                placeholder = "Ej. Mancha foliar",
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            AgroTextField(
                value = confianza,
                onValueChange = { confianza = it },
                label = "Confianza (%)",
                placeholder = "Ej. 92",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            FieldSpacer()

            AgroTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = "Observaciones",
                placeholder = "Notas sobre lo que observaste",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Explica por qué Guardar está deshabilitado
            val faltante =
                when {
                    imagenRuta.isBlank() -> "Agrega una foto para poder guardar."
                    resultado.isBlank() -> "Escribe el resultado del diagnóstico."
                    !confianzaValida -> "La confianza debe ser un número entre 0 y 100."
                    else -> null
                }

            faltante?.let {

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgroTextTertiary
                )
            }
        }
    }

    // ==================================================
    // DELETE
    // ==================================================

    diagnosticoAEliminar?.let { diagnostico ->

        AgroDialog(
            title = "Eliminar diagnóstico",
            confirmText = "Eliminar",
            destructive = true,
            onDismiss = {
                diagnosticoAEliminar = null
            },
            onConfirm = {
                diagnosticoViewModel
                    .eliminarDiagnostico(
                        diagnostico
                    )

                diagnosticoAEliminar =
                    null
            }
        ) {

            Text(
                text = "¿Deseas eliminar el diagnóstico \"${NombresClases.legible(diagnostico.resultado)}\"?",
                style = MaterialTheme.typography.bodyMedium,
                color = AgroTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodySmall,
                color = AgroTextSecondary
            )
        }
    }
}

/** Top-3 del modelo con barra de proporción. Es una sugerencia: el usuario la revisa antes de guardar. */
@Composable
private fun ResultadosModelo(
    predicciones: List<Prediccion>
) {

    AgroCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(R.drawable.ic_sparkle),
                contentDescription = null,
                tint = AgroGreen,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            AgroOverline(text = "Sugerencia del modelo")
        }

        predicciones.forEachIndexed { indice, prediccion ->

            Spacer(modifier = Modifier.height(if (indice == 0) 12.dp else 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = NombresClases.legible(prediccion.clase),
                    style =
                        if (indice == 0) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                    color = if (indice == 0) AgroTextPrimary else AgroTextSecondary,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = prediccion.porcentaje(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (indice == 0) AgroGreenDark else AgroTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AgroProgressBar(
                fraction = prediccion.confianza,
                color = if (indice == 0) AgroGreen else AgroTextTertiary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Revísala antes de guardar.",
            style = MaterialTheme.typography.bodySmall,
            color = AgroTextTertiary
        )
    }
}

@Composable
private fun DiagnosticoCard(
    diagnostico: Diagnostico,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    val miniatura = remember(diagnostico.imagenRuta) {
        cargarMiniatura(diagnostico.imagenRuta)
    }

    val porcentaje =
        (diagnostico.confianza * 100)
            .toInt()
            .coerceIn(0, 100)

    AgroCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (miniatura != null) {

                Image(
                    bitmap = miniatura.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

            } else {

                AgroIconBadge(
                    icon = R.drawable.ic_leaf,
                    size = 56.dp,
                    iconSize = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                AgroOverline(
                    text = etiquetaModulo(diagnostico.modulo),
                    color = AgroTextTertiary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = NombresClases.legible(diagnostico.resultado),
                    style = MaterialTheme.typography.titleMedium,
                    color = AgroTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AgroStatusChip(
                text = "$porcentaje%",
                tone = tonoConfianza(porcentaje)
            )
        }

        diagnostico.observaciones
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AgroTextSecondary
                )
            }

        Spacer(modifier = Modifier.height(14.dp))

        AgroCardActions(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

/** Carga la foto reducida (1/4) para la lista, sin decodificar la imagen completa. */
private fun cargarMiniatura(
    ruta: String
): Bitmap? {

    if (ruta.isBlank() || !File(ruta).exists()) {
        return null
    }

    val opciones = BitmapFactory.Options().apply {
        inSampleSize = 4
    }

    return BitmapFactory.decodeFile(ruta, opciones)
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