package com.agrosense.app.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Parcela
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

    var parcelaSeleccionada by remember {
        mutableStateOf<Parcela?>(null)
    }

    var menuParcelasAbierto by remember {
        mutableStateOf(false)
    }

    var imagenBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var imagenRuta by remember {
        mutableStateOf<String?>(null)
    }

    var resultado by remember {
        mutableStateOf("")
    }

    var confianzaTexto by remember {
        mutableStateOf("")
    }

    var observaciones by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    // Cámara
    val camaraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {

                imagenBitmap = bitmap

                imagenRuta =
                    guardarBitmapEnInterno(
                        context = context,
                        bitmap = bitmap
                    )

                mensaje = null
            }
        }

    // Galería
    val galeriaLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                val bitmap =
                    uriABitmap(
                        context = context,
                        uri = uri
                    )

                if (bitmap != null) {

                    imagenBitmap = bitmap

                    imagenRuta =
                        guardarBitmapEnInterno(
                            context = context,
                            bitmap = bitmap
                        )

                    mensaje = null
                }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp)
    ) {

        Text(
            text = "Nuevo diagnóstico",
            style =
                MaterialTheme
                    .typography
                    .headlineMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text =
                "Selecciona una parcela y toma o carga una fotografía del cultivo.",
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // PARCELA

        Text(
            text = "Parcela",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {
                menuParcelasAbierto = true
            },
            enabled =
                parcelas.isNotEmpty(),
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    parcelaSeleccionada
                        ?.let {
                            "${it.nombre} · ${it.cultivo}"
                        }
                        ?: "Seleccionar parcela"
            )
        }

        DropdownMenu(
            expanded =
                menuParcelasAbierto,
            onDismissRequest = {
                menuParcelasAbierto = false
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

                        parcelaSeleccionada =
                            parcela

                        menuParcelasAbierto =
                            false

                        mensaje = null
                    }
                )
            }
        }

        if (parcelas.isEmpty()) {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Primero registra una parcela.",
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // IMAGEN

        Text(
            text = "Imagen del cultivo",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Button(
            onClick = {
                camaraLauncher.launch(null)
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("Tomar fotografía")
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = {
                galeriaLauncher.launch(
                    "image/*"
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("Elegir de galería")
        }

        imagenBitmap?.let { bitmap ->

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Image(
                bitmap =
                    bitmap.asImageBitmap(),
                contentDescription =
                    "Imagen seleccionada",
                contentScale =
                    ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Imagen lista para analizar ✓",
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // RESULTADO TEMPORAL

        Text(
            text = "Resultado",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                "Por ahora ingresaremos el resultado manualmente. Después lo generará el modelo de IA.",
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = resultado,
            onValueChange = {
                resultado = it
            },
            label = {
                Text(
                    "Resultado del diagnóstico"
                )
            },
            placeholder = {
                Text(
                    "Ej. Tizón temprano"
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
            value = confianzaTexto,
            onValueChange = {
                confianzaTexto = it
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
            value = observaciones,
            onValueChange = {
                observaciones = it
            },
            label = {
                Text("Observaciones")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // GUARDAR

        Button(
            onClick = {

                val parcela =
                    parcelaSeleccionada

                val confianza =
                    confianzaTexto
                        .replace(",", ".")
                        .toFloatOrNull()

                when {

                    parcela == null -> {

                        mensaje =
                            "Selecciona una parcela."
                    }

                    imagenRuta == null -> {

                        mensaje =
                            "Toma o selecciona una fotografía."
                    }

                    resultado.isBlank() -> {

                        mensaje =
                            "Ingresa el resultado del diagnóstico."
                    }

                    confianza == null ||
                            confianza !in 0f..100f -> {

                        mensaje =
                            "La confianza debe estar entre 0 y 100."
                    }

                    else -> {

                        val diagnostico =
                            Diagnostico(
                                parcelaId =
                                    parcela.id,

                                imagenRuta =
                                    imagenRuta!!,

                                resultado =
                                    resultado.trim(),

                                confianza =
                                    confianza / 100f,

                                top3 =
                                    emptyList(),

                                observaciones =
                                    observaciones
                                        .trim()
                                        .ifBlank {
                                            null
                                        }
                            )

                        diagnosticoViewModel
                            .registrarDiagnostico(
                                diagnostico
                            )

                        resultado = ""
                        confianzaTexto = ""
                        observaciones = ""
                        imagenBitmap = null
                        imagenRuta = null

                        mensaje =
                            "Diagnóstico guardado correctamente."
                    }
                }
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "Guardar diagnóstico"
            )
        }

        mensaje?.let { texto ->

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = texto,
                color =
                    if (
                        texto.contains(
                            "correctamente"
                        )
                    ) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .error
                    }
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        OutlinedButton(
            onClick =
                onBackClick,
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                "Volver al inicio"
            )
        }

        Spacer(
            modifier =
                Modifier.height(32.dp)
        )
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
            ?.use { inputStream ->

                BitmapFactory
                    .decodeStream(
                        inputStream
                    )
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
): String? {

    return try {

        val carpeta =
            File(
                context.filesDir,
                "diagnosticos"
            )

        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val archivo =
            File(
                carpeta,
                "diagnostico_${System.currentTimeMillis()}.jpg"
            )

        FileOutputStream(
            archivo
        ).use { outputStream ->

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                outputStream
            )
        }

        archivo.absolutePath

    } catch (
        exception: Exception
    ) {

        null
    }
}