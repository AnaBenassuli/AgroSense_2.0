package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NombresClasesTest {

    private fun etiquetas(modulo: Modulo): List<String> =
        File("src/main/assets", ArchivosModelo.de(modulo).etiquetas)
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    @Test
    fun `toda etiqueta de los modelos tiene nombre en espanol`() {
        // Si se reentrena con otras clases, esta prueba avisa antes de que la app muestre
        // el nombre interno en inglés
        Modulo.entries.flatMap { etiquetas(it) }.forEach { etiqueta ->
            assertTrue("Falta el nombre en español de '$etiqueta'", NombresClases.tieneNombre(etiqueta))
            assertFalse("'$etiqueta' quedó con guiones bajos", NombresClases.legible(etiqueta).contains("_"))
        }
    }

    @Test
    fun `los nombres son los acordados`() {
        assertEquals("Tomate: tizón tardío", NombresClases.legible("Tomato___Late_blight"))
        assertEquals("Maíz: hoja sana", NombresClases.legible("Corn_(maize)___healthy"))
        assertEquals("Bovino: dermatosis nodular contagiosa", NombresClases.legible("Cattle___Lumpy_skin_disease"))
    }

    @Test
    fun `un texto que no es etiqueta del modelo se deja igual`() {
        // El resultado es editable: lo que escriba el usuario, o un nombre ya traducido, no se toca
        assertEquals("Mancha foliar", NombresClases.legible("Mancha foliar"))
        assertEquals("Tomate: tizón tardío", NombresClases.legible("Tomate: tizón tardío"))
        assertEquals("", NombresClases.legible(""))
    }
}
