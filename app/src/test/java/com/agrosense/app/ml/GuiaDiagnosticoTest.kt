package com.agrosense.app.ml

import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GuiaDiagnosticoTest {

    @Test
    fun `el nivel de confianza respeta los umbrales`() {
        assertEquals(NivelConfianza.ALTA, NivelConfianza.de(0.95f))
        assertEquals(NivelConfianza.ALTA, NivelConfianza.de(0.80f))
        assertEquals(NivelConfianza.MEDIA, NivelConfianza.de(0.79f))
        assertEquals(NivelConfianza.MEDIA, NivelConfianza.de(0.50f))
        assertEquals(NivelConfianza.BAJA, NivelConfianza.de(0.49f))
        assertEquals(NivelConfianza.BAJA, NivelConfianza.de(0f))
    }

    @Test
    fun `cada modulo tiene pasos y aviso`() {
        Modulo.entries.forEach { modulo ->
            assertTrue(GuiaDiagnostico.pasos(modulo).size >= 3)
            assertTrue(GuiaDiagnostico.queReconoce(modulo).isNotBlank())
            assertTrue(GuiaDiagnostico.avisoIA(modulo).contains("puede equivocarse"))
        }
        assertTrue(GuiaDiagnostico.avisoIA(Modulo.CULTIVO).contains("agrónomo"))
        assertTrue(GuiaDiagnostico.avisoIA(Modulo.GANADO).contains("veterinario"))
    }

    @Test
    fun `cultivo enfermo pide confirmar con un agronomo antes de aplicar productos`() {
        val lectura = GuiaDiagnostico.leer(Modulo.CULTIVO, Prediccion("Tomato___Late_blight", 0.9f))

        assertEquals(NivelConfianza.ALTA, lectura.nivel)
        assertTrue(lectura.consejo.contains("agrónomo"))
    }

    @Test
    fun `cultivo sano sugiere revisar otras plantas`() {
        val lectura = GuiaDiagnostico.leer(Modulo.CULTIVO, Prediccion("Potato___healthy", 0.6f))

        assertEquals(NivelConfianza.MEDIA, lectura.nivel)
        assertTrue(lectura.consejo.contains("otras plantas"))
    }

    @Test
    fun `sospecha de dermatosis nodular pide separar al animal y llamar al veterinario`() {
        val lectura = GuiaDiagnostico.leer(Modulo.GANADO, Prediccion("Cattle___Lumpy_skin_disease", 0.3f))

        assertEquals(NivelConfianza.BAJA, lectura.nivel)
        assertTrue(lectura.consejo.contains("separa al animal"))
        assertTrue(lectura.consejo.contains("veterinario"))
    }

    @Test
    fun `ganado sin signos aclara que no significa sano`() {
        val lectura = GuiaDiagnostico.leer(Modulo.GANADO, Prediccion("Cattle___healthy", 0.9f))

        assertTrue(lectura.consejo.contains("no quiere decir que el animal esté sano"))
    }

    @Test
    fun `cada modelo tiene una clase sana`() {
        // El consejo distingue sano de enfermo por el sufijo "___healthy" de las etiquetas
        Modulo.entries.forEach { modulo ->
            val sanas = File("src/main/assets", ArchivosModelo.de(modulo).etiquetas)
                .readLines()
                .map { it.trim() }
                .count { it.endsWith("___healthy") }
            assertTrue("$modulo debería tener al menos una clase sana", sanas >= 1)
        }
    }

    @Test
    fun `el conteo de enfermedades de cultivo coincide con las etiquetas`() {
        val enfermedades = File("src/main/assets", ArchivosModelo.de(Modulo.CULTIVO).etiquetas)
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.endsWith("___healthy") }
            .size

        assertTrue(GuiaDiagnostico.queReconoce(Modulo.CULTIVO).contains("$enfermedades enfermedades"))
    }
}
