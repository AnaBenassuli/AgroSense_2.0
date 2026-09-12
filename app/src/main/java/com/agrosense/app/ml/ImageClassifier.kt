package com.agrosense.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.agrosense.app.data.local.entity.Prediccion
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier as TfLiteImageClassifier
import java.io.Closeable
import java.util.Locale

/**
 * Clasificador de enfermedades de cultivo que corre en el dispositivo.
 *
 * El modelo se lee de assets/[MODELO]; si todavía no está copiado (T6.3),
 * [disponible] queda en false y [classify] devuelve un resultado vacío en
 * lugar de tumbar la app, para que el resto del flujo se pueda probar sin
 * el .tflite.
 */
class ImageClassifier(context: Context) : Closeable {

    private val etiquetas: List<String> = cargarEtiquetas(context)
    private var classifier: TfLiteImageClassifier? = null

    /** true cuando el modelo se cargó y se puede clasificar. */
    val disponible: Boolean get() = classifier != null

    init {
        classifier = try {
            val opciones = TfLiteImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(BaseOptions.builder().setNumThreads(NUM_HILOS).build())
                .setMaxResults(MAX_RESULTADOS)
                .setScoreThreshold(UMBRAL_MINIMO)
                .build()

            TfLiteImageClassifier.createFromFileAndOptions(context, MODELO, opciones)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar $MODELO: ${e.message}")
            null
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        val modelo = classifier ?: return ClassificationResult.NO_DISPONIBLE

        return try {
            val entrada = TensorImage.fromBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, false))
            val categorias = modelo.classify(entrada)
                .firstOrNull()
                ?.categories
                .orEmpty()
                .sortedByDescending { it.score }

            if (categorias.isEmpty()) {
                return ClassificationResult.SIN_RESULTADO
            }

            val predicciones = categorias.take(MAX_RESULTADOS).map { categoria ->
                Prediccion(
                    clase = nombreLegible(categoria.label, categoria.index),
                    confianza = categoria.score
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
        classifier?.close()
        classifier = null
    }

    /**
     * El nombre viene de los metadatos del modelo. Si el .tflite se exportó
     * sin ellos, la Task Library entrega el índice como etiqueta; en ese
     * caso se recurre a labels.txt.
     */
    private fun nombreLegible(etiqueta: String?, indice: Int): String {
        val desdeModelo = etiqueta?.takeIf { it.isNotBlank() && it.toIntOrNull() == null }
        val desdeArchivo = etiquetas.getOrNull(indice)
        return desdeModelo ?: desdeArchivo ?: "Clase $indice"
    }

    private fun cargarEtiquetas(context: Context): List<String> = try {
        context.assets.open(ETIQUETAS).bufferedReader().useLines { lineas ->
            lineas.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        private const val TAG = "ImageClassifier"
        const val MODELO = "agrosense_crop_model.tflite"
        const val ETIQUETAS = "labels.txt"
        private const val MAX_RESULTADOS = 3
        private const val UMBRAL_MINIMO = 0.01f
        private const val NUM_HILOS = 2
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
