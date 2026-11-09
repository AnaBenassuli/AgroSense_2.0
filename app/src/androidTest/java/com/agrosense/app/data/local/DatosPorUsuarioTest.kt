package com.agrosense.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.local.entity.Usuario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Cada cuenta ve solo sus parcelas y, a través de ellas, solo sus diagnósticos. */
@RunWith(AndroidJUnit4::class)
class DatosPorUsuarioTest {

    private lateinit var db: AgroSenseDatabase
    private var ana = 0L
    private var luis = 0L

    @Before
    fun crearBase() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AgroSenseDatabase::class.java
        ).build()
        ana = db.usuarioDao().insertar(Usuario(nombre = "Ana", correo = "ana@agrosense.mx"))
        luis = db.usuarioDao().insertar(Usuario(nombre = "Luis", correo = "luis@agrosense.mx"))
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    private suspend fun parcelaDe(usuarioId: Long, nombre: String) =
        db.parcelaDao().insertar(Parcela(nombre = nombre, ubicacion = "Monterrey", cultivo = "Tomate", usuarioId = usuarioId))

    private suspend fun diagnosticoEn(parcelaId: Long) =
        db.diagnosticoDao().insertar(
            Diagnostico(parcelaId = parcelaId, imagenRuta = "/foto.jpg", resultado = "x", confianza = 0.5f)
        )

    @Test
    fun cada_usuario_ve_solo_sus_parcelas() = runBlocking {
        parcelaDe(ana, "Huerta de Ana")
        parcelaDe(luis, "Potrero de Luis")
        parcelaDe(luis, "Milpa de Luis")

        assertEquals(listOf("Huerta de Ana"), db.parcelaDao().observarPorUsuario(ana).first().map { it.nombre })
        assertEquals(
            listOf("Milpa de Luis", "Potrero de Luis"),
            db.parcelaDao().observarPorUsuario(luis).first().map { it.nombre }
        )
    }

    @Test
    fun una_cuenta_nueva_empieza_sin_parcelas_ni_diagnosticos() = runBlocking {
        diagnosticoEn(parcelaDe(ana, "Huerta de Ana"))
        val nueva = db.usuarioDao().insertar(Usuario(nombre = "Nueva", correo = "nueva@agrosense.mx"))

        assertTrue(db.parcelaDao().observarPorUsuario(nueva).first().isEmpty())
        assertEquals(0, db.diagnosticoDao().observarTotalPorUsuario(nueva).first())
    }

    @Test
    fun el_total_de_diagnosticos_es_por_usuario() = runBlocking {
        val huerta = parcelaDe(ana, "Huerta de Ana")
        diagnosticoEn(huerta)
        diagnosticoEn(huerta)
        diagnosticoEn(parcelaDe(luis, "Potrero de Luis"))

        assertEquals(2, db.diagnosticoDao().observarTotalPorUsuario(ana).first())
        assertEquals(1, db.diagnosticoDao().observarTotalPorUsuario(luis).first())
    }
}
