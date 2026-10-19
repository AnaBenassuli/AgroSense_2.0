package com.agrosense.app.ml

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.entity.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Carga los modelos reales de assets/ con el intérprete de Android. Es la única prueba que
 * confirma que el .tflite de ganado (float16) corre en el dispositivo y no solo en Colab.
 */
@RunWith(AndroidJUnit4::class)
class ImageClassifierTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun fotoDePrueba(): Bitmap =
        Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until width) for (y in 0 until height) {
                setPixel(x, y, Color.rgb((x * 7) % 256, (y * 5) % 256, ((x + y) * 3) % 256))
            }
        }

    @Test
    fun el_modelo_de_cultivo_carga_y_devuelve_etiquetas_de_cultivo() {
        ImageClassifier(context, Modulo.CULTIVO).use { clasificador ->
            assertTrue("El modelo de cultivo no cargó", clasificador.disponible)

            val resultado = clasificador.classify(fotoDePrueba())

            assertTrue(resultado.esValido)
            assertTrue(resultado.top3.none { it.clase.startsWith("Cattle") })
        }
    }

    @Test
    fun el_modelo_de_ganado_carga_y_devuelve_sus_dos_clases() {
        ImageClassifier(context, Modulo.GANADO).use { clasificador ->
            assertTrue("El modelo de ganado no cargó", clasificador.disponible)

            val resultado = clasificador.classify(fotoDePrueba())

            assertTrue(resultado.esValido)
            assertTrue(resultado.top3.size <= 2)
            assertTrue(resultado.top3.all { it.clase.startsWith("Cattle___") })
            assertEquals(1f, resultado.top3.sumOf { it.confianza.toDouble() }.toFloat(), 0.02f)
        }
    }

    @Test
    fun el_modulo_por_defecto_sigue_siendo_cultivo() {
        ImageClassifier(context).use { clasificador ->
            assertEquals(Modulo.CULTIVO, clasificador.modulo)
        }
    }
}
