package com.agrosense.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Parcela
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticoModuloTest {

    private lateinit var db: AgroSenseDatabase
    private var parcelaId = 0L

    @Before
    fun crearBase() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AgroSenseDatabase::class.java
        ).build()
        parcelaId = db.parcelaDao().insertar(Parcela(nombre = "Potrero 1", ubicacion = "Monterrey", cultivo = "Ganado"))
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    private fun diagnostico(modulo: Modulo? = null) =
        if (modulo == null) {
            Diagnostico(parcelaId = parcelaId, imagenRuta = "/foto.jpg", resultado = "x", confianza = 0.5f)
        } else {
            Diagnostico(parcelaId = parcelaId, imagenRuta = "/foto.jpg", resultado = "x", confianza = 0.5f, modulo = modulo)
        }

    @Test
    fun un_diagnostico_de_ganado_se_lee_como_ganado() = runBlocking {
        val id = db.diagnosticoDao().insertar(diagnostico(Modulo.GANADO))

        assertEquals(Modulo.GANADO, db.diagnosticoDao().obtenerPorId(id)!!.modulo)
    }

    @Test
    fun sin_indicar_modulo_queda_como_cultivo() = runBlocking {
        val id = db.diagnosticoDao().insertar(diagnostico())

        assertEquals(Modulo.CULTIVO, db.diagnosticoDao().obtenerPorId(id)!!.modulo)
    }
}
