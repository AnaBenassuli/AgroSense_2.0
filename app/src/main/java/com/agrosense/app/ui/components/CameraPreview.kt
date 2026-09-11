package com.agrosense.app.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.agrosense.app.local.ImagenStorage

/**
 * Vista previa de la cámara con botón de captura.
 *
 * La foto se guarda en el almacenamiento privado de la app y se devuelve en
 * [onPhotoCaptured] como URI file://, lista para mostrarse y para guardarse
 * en Room.
 *
 * Quien la use debe haber concedido ya el permiso de cámara.
 */
@Composable
fun CameraPreview(
    onPhotoCaptured: (String) -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var capturando by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null

        future.addListener({
            try {
                val camaras = future.get()
                provider = camaras
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                camaras.unbindAll()
                camaras.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                onError(e.message ?: "No se pudo abrir la cámara")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose { provider?.unbindAll() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (capturando) return@Button
                    capturando = true

                    val archivo = ImagenStorage.nuevoArchivoFoto(context)
                    val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

                    imageCapture.takePicture(
                        opciones,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                                capturando = false
                                onPhotoCaptured(ImagenStorage.comoUri(archivo))
                            }

                            override fun onError(excepcion: ImageCaptureException) {
                                capturando = false
                                archivo.delete()
                                onError(excepcion.message ?: "No se pudo guardar la foto")
                            }
                        }
                    )
                },
                enabled = !capturando,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (capturando) "Capturando…" else "Capturar")
            }
        }
    }
}
