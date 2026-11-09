package com.agrosense.app.ui.screen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
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
import com.agrosense.app.ui.components.AgroDialogo
import com.agrosense.app.ui.components.Avisos
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
import androidx.compose.runtime.rememberCoroutineScope
import com.agrosense.app.data.local.ImagenStorage
import com.agrosense.app.ui.validacion.leerPorcentaje
import com.agrosense.app.ui.validacion.validarDiagnostico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.ui.components.AvisoIA
import com.agrosense.app.ui.components.BotonRegresar
import com.agrosense.app.ui.components.GuiaFoto
import com.agrosense.app.ui.components.LecturaIA
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import com.agrosense.app.ml.ImageClassifier
import com.agrosense.app.ml.NombresClases
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.ParcelaViewModel

/**
 * @param modulo módulo con el que abre el formulario. Dentro del formulario se puede cambiar con
 *   el selector Cultivo | Ganado; la pantalla de selección de módulo (tarea 31) puede pasar
 *   GANADO aquí para que abra directo en ganado.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiagnosticoScreen(
    diagnosticoViewModel: DiagnosticoViewModel,
    parcelaViewModel: ParcelaViewModel,
    onBackClick: () -> Unit,
    modulo: Modulo = Modulo.CULTIVO,
    onIrAParcelas: () -> Unit = {}
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

            remember(id) {
                diagnosticoViewModel.observarPorParcela(id)
            }.collectAsState(
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

    // true cuando la foto en pantalla todavía no está en disco. Se guarda recién al tocar Guardar:
    // antes se guardaba al elegirla y, si se cancelaba el formulario, quedaba huérfana.
    var fotoNueva by remember {
        mutableStateOf(false)
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

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

    // Los pasos para tomar la foto se muestran abiertos al empezar y se pliegan al tener foto,
    // para que el formulario no se alargue de más; se pueden volver a abrir.
    // Cada análisis sube este contador; la explicación del resultado queda debajo del botón,
    // fuera de la pantalla, así que al cambiar se desplaza el formulario hasta ella
    var analisisHechos by remember {
        mutableStateOf(0)
    }

    val verLecturaIA = remember {
        BringIntoViewRequester()
    }

    LaunchedEffect(analisisHechos) {
        if (analisisHechos > 0) {
            verLecturaIA.bringIntoView()
        }
    }

    var verPasosFoto by remember {
        mutableStateOf(true)
    }

    // Una foto nueva invalida el análisis anterior, que era de otra imagen
    fun usarFotoNueva(foto: Bitmap) {
        imagenBitmap = foto
        fotoNueva = true
        mensajeError = null
        top3Detectado = emptyList()
        mensajeAnalisis = null
        verPasosFoto = false
    }

    val camaraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            bitmap?.let {
                usarFotoNueva(ImagenStorage.reducir(it))
            }
        }

    val galeriaLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {
                // Decodificar una foto de 12 MP tarda; fuera del hilo de la interfaz
                scope.launch {
                    val foto = withContext(Dispatchers.IO) {
                        ImagenStorage.cargarDesdeUri(context, it)
                    }

                    if (foto == null) {
                        mensajeError = "No se pudo abrir esa foto. Prueba con otra."
                    } else {
                        usarFotoNueva(foto)
                    }
                }
            }
        }

    fun limpiarFormulario() {
        resultado = ""
        confianza = ""
        observaciones = ""
        imagenBitmap = null
        imagenRuta = ""
        fotoNueva = false
        mensajeError = null
        diagnosticoEditando = null
        top3Detectado = emptyList()
        mensajeAnalisis = null
        verPasosFoto = true
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

        // LecturaIA lo explica en pantalla a partir del top-3
        top3Detectado = analisis.top3

        analisisHechos++

        mensajeAnalisis = null
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

        imagenBitmap = null
        fotoNueva = false
        mensajeError = null

        scope.launch {
            imagenBitmap = withContext(Dispatchers.IO) {
                ImagenStorage.cargarDesdeArchivo(diagnostico.imagenRuta)
            }
        }

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

            BotonRegresar(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 12.dp)
            )

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
                    "Los diagnósticos se guardan dentro de una parcela (tu terreno o potrero). " +
                        "Registra una y vuelve aquí."
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Button(
                onClick = onIrAParcelas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape =
                    RoundedCornerShape(14.dp)
            ) {

                Text("Ir a Mis parcelas")
            }

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

        AgroDialogo(
            onDismissRequest = {
                mostrarFormulario =
                    false
                limpiarFormulario()
            },

            shape =
                RoundedCornerShape(18.dp),

            title = {

                Column {

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

                    // Junto al título y no al final del formulario: con foto, el formulario es más
                    // largo que la pantalla y un mensaje abajo quedaría fuera de la vista
                    mensajeError?.let { error ->

                        Text(
                            text = error,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp
                                )
                        )
                    }
                }
            },

            text = {

                LazyColumn {

                    item {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            listOf(
                                Modulo.CULTIVO to "Cultivo",
                                Modulo.GANADO to "Ganado"
                            ).forEach { (opcion, etiqueta) ->

                                if (opcion == moduloActual) {
                                    Button(
                                        onClick = {},
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {
                                        Text(etiqueta)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            cambiarModulo(opcion)
                                        },
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {
                                        Text(etiqueta)
                                    }
                                }
                            }
                        }

                        GuiaFoto(
                            modulo = moduloActual,
                            expandida = verPasosFoto,
                            onAlternar = {
                                verPasosFoto = !verPasosFoto
                            },
                            modifier =
                                Modifier.padding(
                                    top = 10.dp
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

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

                        AvisoIA(
                            modulo = moduloActual
                        )

                        // Errores del análisis (sin foto, sin resultado, modelo con fallas)
                        mensajeAnalisis?.let { texto ->

                            Text(
                                text = texto,
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error,
                                modifier =
                                    Modifier.padding(
                                        top = 8.dp
                                    )
                            )
                        }

                        if (top3Detectado.isNotEmpty()) {

                            LecturaIA(
                                modulo = moduloActual,
                                top3 = top3Detectado,
                                modifier =
                                    Modifier
                                        .padding(
                                            top = 10.dp
                                        )
                                        .bringIntoViewRequester(
                                            verLecturaIA
                                        )
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
                    enabled = !guardando,
                    onClick = {

                        val parcelaId =
                            parcelaSeleccionadaId

                        val foto =
                            imagenBitmap

                        mensajeError =
                            validarDiagnostico(
                                hayParcela = parcelaId != null,
                                hayFoto = foto != null || imagenRuta.isNotBlank(),
                                resultado = resultado,
                                confianza = confianza
                            )

                        val porcentaje =
                            leerPorcentaje(confianza)

                        if (
                            mensajeError == null &&
                            parcelaId != null &&
                            porcentaje != null
                        ) {

                            guardando = true

                            scope.launch {

                                val ruta =
                                    if (fotoNueva && foto != null) {
                                        withContext(Dispatchers.IO) {
                                            ImagenStorage.guardar(context, foto)
                                        }
                                    } else {
                                        imagenRuta
                                    }

                                val actual =
                                    diagnosticoEditando

                                if (actual == null) {

                                    diagnosticoViewModel
                                        .registrarDiagnostico(
                                            Diagnostico(
                                                parcelaId = parcelaId,
                                                imagenRuta = ruta,
                                                resultado = resultado.trim(),
                                                confianza = porcentaje / 100f,
                                                top3 = top3Detectado,
                                                observaciones = observaciones.trim().ifBlank { null },
                                                modulo = moduloActual
                                            )
                                        )

                                } else {

                                    diagnosticoViewModel
                                        .actualizarDiagnostico(
                                            actual.copy(
                                                parcelaId = parcelaId,
                                                imagenRuta = ruta,
                                                resultado = resultado.trim(),
                                                confianza = porcentaje / 100f,
                                                top3 = top3Detectado,
                                                observaciones = observaciones.trim().ifBlank { null },
                                                modulo = moduloActual
                                            ),
                                            // Si se cambió la foto, se borra la anterior del disco
                                            rutaImagenAnterior = actual.imagenRuta
                                        )
                                }

                                Avisos.mostrar(if (actual == null) "Diagnóstico guardado" else "Diagnóstico actualizado")

                                guardando = false
                                mostrarFormulario = false
                                limpiarFormulario()
                            }
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

        AgroDialogo(
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
                            "¿Deseas eliminar el diagnóstico \"${NombresClases.legible(diagnostico.resultado)}\"?"
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
                        Avisos.mostrar("Diagnóstico eliminado")

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
                    when (diagnostico.modulo) {
                        Modulo.CULTIVO -> "Cultivo"
                        Modulo.GANADO -> "Ganado"
                    },
                style =
                    MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme.primary
            )

            Text(
                text =
                    NombresClases.legible(diagnostico.resultado),
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
