package com.agrosense.app.ui.screen.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTypeTest {

    @Test
    fun `cada tipo se recupera por la etiqueta que se guarda en Room`() {
        UserType.entries.forEach { tipo ->
            assertEquals(tipo, UserType.desdeEtiqueta(tipo.label))
        }
    }

    @Test
    fun `una etiqueta desconocida o vacia se lee como productor`() {
        assertEquals(UserType.PRODUCER, UserType.desdeEtiqueta(""))
        assertEquals(UserType.PRODUCER, UserType.desdeEtiqueta("Ganadero"))
    }

    @Test
    fun `todos los tipos tienen explicacion`() {
        assertTrue(UserType.entries.all { it.descripcion.isNotBlank() })
    }
}
