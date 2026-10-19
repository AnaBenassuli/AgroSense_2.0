package com.agrosense.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/** Modelo y etiquetas de un módulo, como se llaman en assets/. */
data class ArchivosModelo(val modelo: String, val etiquetas: String) {
    companion object {
        fun de(modulo: Modulo): ArchivosModelo = when (modulo) {
            Modulo.CULTIVO -> ArchivosModelo("agrosense_crop_model.tflite", "labels.txt")
            Modulo.GANADO -> ArchivosModelo("agrosense_ganado_model.tflite", "labels_ganado.txt")
        }
    }
}

/**
 * Clasificador de enfermedades que corre en el dispositivo, uno por [Modulo]:
 * cultivos (hojas de maíz, papa y tomate) o ganado (dermatosis nodular).
 *
 * Usa el `Interpreter` de TensorFlow Lite directamente, no la Task Library:
 * ésta exige que el .tflite lleve metadata embebida, y la herramienta que la
 * escribe (`tflite-support`) ya no publica versiones compatibles con el Python
 * que usa Colab hoy. Con el intérprete, el modelo que exporta el notebook sirve
 * tal cual y las etiquetas salen del .txt de [ArchivosModelo].
 *
 * A cambio, el preprocesamiento pasa a ser responsabilidad de esta clase: ver
 * [prepararEntrada]. Los dos notebooks entrenan con MobileNetV2, así que el
 * preprocesamiento es el mismo para ambos módulos.
 *
 * Si el modelo todavía no está en assets, [disponible] queda en false y
 * [classify] devuelve un resultado vacío en lugar de tumbar la app, para que el
 * resto del flujo se pueda probar sin el .tflite.
 */
class ImageClassifier(
    context: Context,
    val modulo: Modulo = Modulo.CULTIVO
) : Closeable {

    // Antes que las etiquetas: cargarEtiquetas lo necesita durante la inicialización
    private val archivos = ArchivosModelo.de(modulo)
    private val etiquetas: List<String> = cargarEtiquetas(context)
    private var interprete: Interpreter? = null
    private var ladoEntrada: Int = 0
    private var numClases: Int = 0

    /** true cuando el modelo se cargó y se puede clasificar. */
    val disponible: Boolean get() = interprete != null

    init {
        interprete = try {
            val opciones = Interpreter.Options().apply { numThreads = NUM_HILOS }
            val creado = Interpreter(cargarModelo(context), opciones)

            // La forma la dicta el modelo, no la fijamos aquí: [1, lado, lado, 3]
            // a la entrada y [1, clases] a la salida.
            ladoEntrada = creado.getInputTensor(0).shape()[1]
            val formaSalida = creado.getOutputTensor(0).shape()
            numClases = formaSalida[formaSalida.size - 1]

            if (etiquetas.isNotEmpty() && etiquetas.size != numClases) {
                // Un labels.txt descuadrado renombraría todos los diagnósticos
                // sin provocar ningún error. Preferimos no clasificar a mentir.
                Log.e(
                    TAG,
                    "${archivos.etiquetas} tiene ${etiquetas.size} etiquetas pero el modelo " +
                        "devuelve $numClases clases. Clasificador desactivado."
                )
                creado.close()
                null
            } else {
                Log.i(
                    TAG,
                    "Modelo de $modulo cargado: entrada ${ladoEntrada}x$ladoEntrada, $numClases clases."
                )
                creado
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar ${archivos.modelo}: ${e.message}")
            null
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        val modelo = interprete ?: return ClassificationResult.NO_DISPONIBLE

        return try {
            val salida = Array(1) { FloatArray(numClases) }
            modelo.run(prepararEntrada(bitmap), salida)
            val probabilidades = salida[0]

            val mejores = probabilidades.indices
                .sortedByDescending { probabilidades[it] }
                .filter { probabilidades[it] >= UMBRAL_MINIMO }
                .take(MAX_RESULTADOS)

            if (mejores.isEmpty()) {
                return ClassificationResult.SIN_RESULTADO
            }

            val predicciones = mejores.map { indice ->
                Prediccion(
                    clase = nombreLegible(indice),
                    confianza = probabilidades[indice]
                )
            }

            ClassificationResult(
                label = predicciones.first().clase,
                confidence = predicciones.first().confianza,
                top3 = predicciones
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error clasificando la imagen", e)
            ClassificationResult.ERROR
        }
    }

    override fun close() {
        interprete?.close()
        interprete = null
    }

    /**
     * Redimensiona la foto a la entrada del modelo y normaliza a [-1, 1].
     *
     * Los 127.5 vienen de `mobilenet_v2.preprocess_input`, que es lo que aplica
     * el notebook de entrenamiento: (pixel - 127.5) / 127.5. Si allí se cambia
     * de backbone, hay que cambiar estas constantes o el modelo recibirá los
     * píxeles en un rango que no espera.
     */
    private fun prepararEntrada(bitmap: Bitmap): ByteBuffer {
        val lado = ladoEntrada
        val rgb = if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
        val escalado = Bitmap.createScaledBitmap(rgb, lado, lado, true)

        val buffer = ByteBuffer
            .allocateDirect(lado * lado * CANALES * BYTES_POR_FLOAT)
            .order(ByteOrder.nativeOrder())

        val pixeles = IntArray(lado * lado)
        escalado.getPixels(pixeles, 0, lado, 0, 0, lado, lado)
        for (pixel in pixeles) {
            buffer.putFloat((((pixel shr 16) and 0xFF) - MEDIA) / DESVIACION)
            buffer.putFloat((((pixel shr 8) and 0xFF) - MEDIA) / DESVIACION)
            buffer.putFloat(((pixel and 0xFF) - MEDIA) / DESVIACION)
        }
        buffer.rewind()

        if (escalado !== rgb) escalado.recycle()
        if (rgb !== bitmap) rgb.recycle()
        return buffer
    }

    /**
     * El intérprete solo devuelve índices, así que el nombre sale siempre del
     * archivo de etiquetas. El init ya garantiza que, si el archivo existe, su
     * tamaño coincide con el número de salidas del modelo.
     */
    private fun nombreLegible(indice: Int): String =
        etiquetas.getOrNull(indice) ?: "Clase $indice"

    private fun cargarEtiquetas(context: Context): List<String> = try {
        context.assets.open(archivos.etiquetas).bufferedReader().useLines { lineas ->
            lineas.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo leer ${archivos.etiquetas}: ${e.message}")
        emptyList()
    }

    private fun cargarModelo(context: Context): MappedByteBuffer =
        context.assets.openFd(archivos.modelo).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { entrada ->
                entrada.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
            }
        }

    companion object {
        private const val TAG = "ImageClassifier"
        private const val MAX_RESULTADOS = 3
        private const val UMBRAL_MINIMO = 0.01f
        private const val NUM_HILOS = 2
        private const val CANALES = 3
        private const val BYTES_POR_FLOAT = 4
        private const val MEDIA = 127.5f
        private const val DESVIACION = 127.5f
    }
}

data class ClassificationResult(
    val label: String,
    val confidence: Float,
    val top3: List<Prediccion>
) {
    val esValido: Boolean get() = top3.isNotEmpty()

    companion object {
        val NO_DISPONIBLE = ClassificationResult("Modelo no disponible", 0f, emptyList())
        val SIN_RESULTADO = ClassificationResult("Sin resultado", 0f, emptyList())
        val ERROR = ClassificationResult("Error al analizar", 0f, emptyList())
    }
}

/**
 * Confianza como texto para mostrar en pantalla, p. ej. "91.2%".
 *
 * Fijo en Locale.US a propósito: en un teléfono en español el separador
 * decimal por defecto es la coma, y eso desalinearía este texto con el de
 * [Prediccion.porcentaje], que usa el mismo formato.
 */
fun porcentaje(valor: Float): String = String.format(Locale.US, "%.1f%%", valor * 100)

/** Confianza como texto para mostrar en pantalla, p. ej. "91.2%". */
fun Prediccion.porcentaje(): String = porcentaje(confianza)
