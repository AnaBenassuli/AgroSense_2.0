package com.agrosense.app.data.local.converter

import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `el top3 sobrevive el ida y vuelta`() {
        val original = listOf(
            Prediccion("Tomato___Bacterial_spot", 0.91f),
            Prediccion("Tomato___Early_blight", 0.06f),
            Prediccion("Potato___healthy", 0.03f)
        )

        val recuperado = converters.stringToTop3(converters.top3ToString(original))

        assertEquals(original, recuperado)
    }

    @Test
    fun `una lista vacia vuelve vacia`() {
        assertTrue(converters.stringToTop3(converters.top3ToString(emptyList())).isEmpty())
    }

    @Test
    fun `un valor nulo o basura no revienta`() {
        assertTrue(converters.stringToTop3(null).isEmpty())
        assertTrue(converters.stringToTop3("sin separadores").isEmpty())
    }

    @Test
    fun `una clase con los separadores dentro no rompe el formato`() {
        val original = listOf(Prediccion("clase;;rara::con separadores", 0.5f))

        val recuperado = converters.stringToTop3(converters.top3ToString(original))

        assertEquals(1, recuperado.size)
        assertEquals(0.5f, recuperado.first().confianza, 0.0001f)
    }

    @Test
    fun `el modulo se guarda con su nombre y vuelve igual`() {
        Modulo.entries.forEach { modulo ->
            assertEquals(modulo.name, converters.moduloToString(modulo))
            assertEquals(modulo, converters.stringToModulo(converters.moduloToString(modulo)))
        }
    }

    @Test
    fun `un modulo desconocido o nulo se lee como cultivo`() {
        // Las filas anteriores a la v3 son todas de cultivo; un valor raro no debe tumbar la lectura
        assertEquals(Modulo.CULTIVO, converters.stringToModulo(null))
        assertEquals(Modulo.CULTIVO, converters.stringToModulo("ACUICULTURA"))
    }
}
