package com.agrosense.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagenStorageTest {

    @Test
    fun `una foto de 4160x3120 se decodifica a un cuarto para quedar sobre 1024`() {
        // El caso real del teléfono de pruebas: 4160/4 = 1040 >= 1024, 4160/8 = 520 < 1024
        assertEquals(4, ImagenStorage.calcularInSampleSize(4160, 3120, 1024))
    }

    @Test
    fun `una foto ya chica no se reduce`() {
        assertEquals(1, ImagenStorage.calcularInSampleSize(640, 480, 1024))
        assertEquals(1, ImagenStorage.calcularInSampleSize(1024, 768, 1024))
    }

    @Test
    fun `nunca deja la imagen por debajo del lado pedido`() {
        listOf(1500 to 1000, 2048 to 1536, 4000 to 3000, 8000 to 6000, 3120 to 4160).forEach { (w, h) ->
            val muestra = ImagenStorage.calcularInSampleSize(w, h, 1024)
            assertTrue("$w x $h con muestra $muestra", maxOf(w, h) / muestra >= 1024)
        }
    }

    @Test
    fun `dimensiones invalidas no rompen el calculo`() {
        assertEquals(1, ImagenStorage.calcularInSampleSize(0, 0, 1024))
        assertEquals(1, ImagenStorage.calcularInSampleSize(-1, 500, 1024))
    }
}
