package com.agrosense.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.local.ImagenStorage
import com.agrosense.app.ml.ImageClassifier
import com.agrosense.app.navigation.Route
import com.agrosense.app.ui.components.CameraPreview
import com.agrosense.app.ui.viewmodel.DiagnosticoViewModel
import com.agrosense.app.ui.viewmodel.agroSenseViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Captura de la foto a diagnosticar para una parcela.
 *
 * La foto puede venir de la cámara o de la galería; en ambos casos termina
 * copiada en el almacenamiento privado de la app. Al analizar se guarda el
 * diagnóstico en Room y se navega a su pantalla de resultado.
 */
@Composable
fun CapturaDiagnosticoScreen(navController: NavController, parcelaId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val diagnosticoViewModel: DiagnosticoViewModel = viewModel(factory = agroSenseViewModelFactory(context))

    val clasificador = remember(context) { ImageClassifier(context) }
    DisposableEffect(clasificador) {
        onDispose { clasificador.close() }
    }

    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }
    var analizando by remember { mutableStateOf(false) }
    var mostrandoCamara by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun analizar(uri: String) {
        scope.launch {
            analizando = true
            mensaje = null

            val resultado = withContext(Dispatchers.Default) {
                val bitmap = ImagenStorage.cargarBitmap(uri)
                if (bitmap == null) null else clasificador.classify(bitmap)
            }

            when {
                resultado == null -> {
                    mensaje = "No se pudo leer la foto."
                    analizando = false
                }

                !clasificador.disponible -> {
                    mensaje = "El modelo de diagnóstico todavía no está instalado " +
                        "(falta ${ImageClassifier.MODELO} en assets)."
                    analizando = false
                }

                !resultado.esValido -> {
                    mensaje = "El modelo no reconoció nada en esta foto. Intenta " +
                        "con la hoja más cerca y bien iluminada."
                    analizando = false
                }

                else -> {
                    val resultadoEscritura = runCatching {
                        withContext(Dispatchers.IO) {
                            diagnosticoViewModel.registrarYDevolverId(
                                Diagnostico(
                                    parcelaId = parcelaId,
                                    imagenRuta = uri,
                                    resultado = resultado.label,
                                    confianza = resultado.confidence,
                                    top3 = resultado.top3
                                )
                            )
                        }
                    }

                    analizando = false

                    resultadoEscritura
                        .onSuccess { id ->
                            navController.navigate(Route.ResultadoDiagnostico.create(id))
                        }
                        .onFailure {
                            mensaje = "No se pudo guardar el diagnóstico. Intenta nuevamente."
                        }
                }
            }
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            mostrandoCamara = true
        } else {
            mensaje = "Sin permiso de cámara. Puedes elegir una foto de la galería."
        }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val copia = ImagenStorage.copiarDesdeUri(context, uri)
            if (copia != null) {
                imagenSeleccionada = ImagenStorage.comoUri(copia)
                mensaje = null
            } else {
                mensaje = "No se pudo leer la imagen seleccionada."
            }
        }
    }

    BackHandler(enabled = mostrandoCamara) { mostrandoCamara = false }

    if (mostrandoCamara) {
        CameraPreview(
            onPhotoCaptured = { uri ->
                imagenSeleccionada = uri
                mensaje = null
                mostrandoCamara = false
            },
            onCancel = { mostrandoCamara = false },
            onError = {
                mensaje = it
                mostrandoCamara = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Capturar foto", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Parcela $parcelaId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        VistaPreviaFoto(imagenSeleccionada)

        if (mensaje != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                mensaje!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val concedido = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (concedido) {
                    mostrandoCamara = true
                } else {
                    permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tomar foto")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                galeriaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Seleccionar de galería")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { imagenSeleccionada?.let { analizar(it) } },
            enabled = imagenSeleccionada != null && !analizando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (analizando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Analizar foto")
            }
        }
    }
}

@Composable
private fun VistaPreviaFoto(uri: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (uri == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Sin foto", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Toma una foto de la hoja afectada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Foto a diagnosticar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
