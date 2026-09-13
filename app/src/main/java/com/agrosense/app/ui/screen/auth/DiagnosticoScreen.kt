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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val camaraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {

                imagenBitmap = bitmap

                imagenRuta = guardarBitmapEnInterno(
                    context = context,
                    bitmap = bitmap
                )

                mensaje = null
            }
        }

    val galeriaLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                val bitmap = uriABitmap(
                    context = context,
                    uri = uri
                )

                if (bitmap != null) {

                    imagenBitmap = bitmap

                    imagenRuta = guardarBitmapEnInterno(
                        context = context,
                        bitmap = bitmap
                    )

                    mensaje = null
                }
            }
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Text(
                            text = "←",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            color =
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                },

                title = {

                    Column {

                        Text(
                            text = "Nuevo diagnóstico",
                            style =
                                MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Analiza el estado de tu cultivo",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            item {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "1. Selecciona la parcela",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        OutlinedButton(
                            onClick = {
                                menuParcelasAbierto = true
                            },
                            enabled = parcelas.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape =
                                RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text =
                                    parcelaSeleccionada?.let {
                                        "${it.nombre} · ${it.cultivo}"
                                    }
                                        ?: "Seleccionar parcela"
                            )
                        }

                        DropdownMenu(
                            expanded =
                                menuParcelasAbierto,
                            onDismissRequest = {
                                menuParcelasAbierto =
                                    false
                            }
                        ) {

                            parcelas.forEach { parcela ->

                                DropdownMenuItem(
                                    text = {

                                        Text(
                                            text =
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
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {

                Text(
                    text = "2. Agrega una imagen",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        if (imagenBitmap == null) {

                            Text(
                                text =
                                    "Toma una fotografía clara de la hoja o zona afectada.",
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )
                        }

                        imagenBitmap?.let { bitmap ->

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(14.dp)
                            ) {

                                Image(
                                    bitmap =
                                        bitmap.asImageBitmap(),
                                    contentDescription =
                                        "Imagen del cultivo",
                                    contentScale =
                                        ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text = "Imagen lista ✓",
                                style =
                                    MaterialTheme.typography.labelLarge,
                                color =
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(14.dp)
                            )
                        }

                        Button(
                            onClick = {
                                camaraLauncher.launch(null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape =
                                RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Tomar fotografía"
                            )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape =
                                RoundedCornerShape(14.dp)
                        ) {

                            Text(
                                text = "Elegir de galería"
                            )
                        }
                    }
                }
            }

            item {

                Text(
                    text = "3. Resultado",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Resultado temporal",
                            style =
                                MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "Hasta integrar la IA, puedes registrar manualmente el diagnóstico.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )

                        OutlinedTextField(
                            value = resultado,
                            onValueChange = {
                                resultado = it
                            },
                            label = {
                                Text("Resultado")
                            },
                            placeholder = {
                                Text("Ej. Tizón temprano")
                            },
                            singleLine = true,
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(14.dp)
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
                                Text("Confianza (%)")
                            },
                            placeholder = {
                                Text("Ej. 92")
                            },
                            singleLine = true,
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(14.dp)
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
                            placeholder = {
                                Text("Agrega detalles...")
                            },
                            minLines = 3,
                            maxLines = 5,
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            item {

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

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text = "Guardar diagnóstico",
                        style =
                            MaterialTheme.typography.labelLarge
                    )
                }
            }

            mensaje?.let { texto ->

                item {

                    val correcto =
                        texto.contains(
                            "correctamente"
                        )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(14.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (correcto) {
                                        MaterialTheme
                                            .colorScheme
                                            .primaryContainer
                                    } else {
                                        MaterialTheme
                                            .colorScheme
                                            .errorContainer
                                    }
                            )
                    ) {

                        Text(
                            text = texto,
                            modifier =
                                Modifier.padding(14.dp),
                            color =
                                if (correcto) {
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                                }
                        )
                    }
                }
            }

            item {

                Spacer(
                    modifier =
                        Modifier.height(32.dp)
                )
            }
        }
    }
}

private fun uriABitmap(
    context: Context,
    uri: Uri
): Bitmap? {

    return try {

        context.contentResolver
            .openInputStream(uri)
            ?.use { inputStream ->

                BitmapFactory.decodeStream(
                    inputStream
                )
            }

    } catch (exception: Exception) {

        null
    }
}

private fun guardarBitmapEnInterno(
    context: Context,
    bitmap: Bitmap
): String? {

    return try {

        val carpeta = File(
            context.filesDir,
            "diagnosticos"
        )

        if (!carpeta.exists()) {
            carpeta.mkdirs()
        }

        val archivo = File(
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

    } catch (exception: Exception) {

        null
    }
}