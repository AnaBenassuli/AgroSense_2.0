package com.agrosense.app.ui.validacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun `el porcentaje acepta punto, coma, espacios y simbolo de porcentaje`() {
        assertEquals(85.5f, leerPorcentaje("85.5")!!, 0.001f)
        assertEquals(85.5f, leerPorcentaje("85,5")!!, 0.001f)
        assertEquals(90f, leerPorcentaje(" 90 % ")!!, 0.001f)
        assertEquals(0f, leerPorcentaje("0")!!, 0.001f)
        assertEquals(100f, leerPorcentaje("100")!!, 0.001f)
    }

    @Test
    fun `un porcentaje vacio, no numerico o fuera de 0 a 100 no es valido`() {
        assertNull(leerPorcentaje(""))
        assertNull(leerPorcentaje("alta"))
        assertNull(leerPorcentaje("-1"))
        assertNull(leerPorcentaje("100.1"))
    }

    @Test
    fun `un diagnostico completo no tiene errores`() {
        assertNull(validarDiagnostico(hayParcela = true, hayFoto = true, resultado = "Tomate: tizón tardío", confianza = "91"))
    }

    @Test
    fun `cada dato que falta tiene su propio mensaje`() {
        val sinParcela = validarDiagnostico(hayParcela = false, hayFoto = true, resultado = "x", confianza = "50")
        val sinFoto = validarDiagnostico(hayParcela = true, hayFoto = false, resultado = "x", confianza = "50")
        val sinResultado = validarDiagnostico(hayParcela = true, hayFoto = true, resultado = " ", confianza = "50")
        val malaConfianza = validarDiagnostico(hayParcela = true, hayFoto = true, resultado = "x", confianza = "mucha")

        listOf(sinParcela, sinFoto, sinResultado, malaConfianza).forEach { assertNotNull(it) }
        assertEquals(4, setOf(sinParcela, sinFoto, sinResultado, malaConfianza).size)
    }
}
