package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Prediccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassificationResultTest {

    @Test
    fun `un resultado con predicciones es valido`() {
        val resultado = ClassificationResult(
            label = "Tomato___Early_blight",
            confidence = 0.8f,
            top3 = listOf(Prediccion("Tomato___Early_blight", 0.8f))
        )

        assertTrue(resultado.esValido)
    }

    @Test
    fun `los resultados de fallo no son validos`() {
        assertFalse(ClassificationResult.NO_DISPONIBLE.esValido)
        assertFalse(ClassificationResult.SIN_RESULTADO.esValido)
        assertFalse(ClassificationResult.ERROR.esValido)
    }

    @Test
    fun `el porcentaje se formatea con un decimal`() {
        assertEquals("91.2%", Prediccion("x", 0.9123f).porcentaje())
        assertEquals("0.0%", Prediccion("x", 0f).porcentaje())
    }
}
