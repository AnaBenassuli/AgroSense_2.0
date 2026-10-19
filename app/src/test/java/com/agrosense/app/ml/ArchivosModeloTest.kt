package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchivosModeloTest {

    // Las pruebas unitarias corren con app/ como directorio de trabajo
    private val assets = File("src/main/assets")

    private fun etiquetas(modulo: Modulo): List<String> =
        File(assets, ArchivosModelo.de(modulo).etiquetas)
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    @Test
    fun `cultivo sigue usando los archivos de siempre`() {
        val archivos = ArchivosModelo.de(Modulo.CULTIVO)

        assertEquals("agrosense_crop_model.tflite", archivos.modelo)
        assertEquals("labels.txt", archivos.etiquetas)
    }

    @Test
    fun `cada modulo tiene sus propios archivos`() {
        val cultivo = ArchivosModelo.de(Modulo.CULTIVO)
        val ganado = ArchivosModelo.de(Modulo.GANADO)

        assertNotEquals(cultivo.modelo, ganado.modelo)
        assertNotEquals(cultivo.etiquetas, ganado.etiquetas)
    }

    @Test
    fun `los archivos de todos los modulos estan en assets`() {
        Modulo.entries.forEach { modulo ->
            val archivos = ArchivosModelo.de(modulo)
            assertTrue("Falta ${archivos.modelo}", File(assets, archivos.modelo).isFile)
            assertTrue("Falta ${archivos.etiquetas}", File(assets, archivos.etiquetas).isFile)
        }
    }

    @Test
    fun `las etiquetas de ganado son las dos que exporta el notebook, en orden`() {
        assertEquals(
            listOf("Cattle___Lumpy_skin_disease", "Cattle___healthy"),
            etiquetas(Modulo.GANADO)
        )
    }

    @Test
    fun `las etiquetas de cultivo no cambiaron`() {
        val cultivo = etiquetas(Modulo.CULTIVO)

        assertEquals(17, cultivo.size)
        assertTrue(cultivo.none { it.startsWith("Cattle") })
    }
}
