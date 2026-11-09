package com.agrosense.app.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.data.repository.DiagnosticoRepository
import com.agrosense.app.data.repository.ParcelaRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Fotos de diagnósticos: tamaño al guardarlas y que no queden archivos huérfanos. */
@RunWith(AndroidJUnit4::class)
class FotosDiagnosticoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: AgroSenseDatabase
    private lateinit var carpeta: File
    private var parcelaId = 0L

    @Before
    fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, AgroSenseDatabase::class.java).build()
        // Carpeta propia de la prueba, para no tocar las fotos reales de la app
        carpeta = File(context.cacheDir, "fotos-prueba").apply { deleteRecursively(); mkdirs() }
        val usuario = db.usuarioDao().insertar(Usuario(nombre = "Ana", correo = "ana@agrosense.mx"))
        parcelaId = db.parcelaDao().insertar(
            Parcela(nombre = "Huerta", ubicacion = "Monterrey", cultivo = "Tomate", usuarioId = usuario)
        )
    }

    @After
    fun limpiar() {
        db.close()
        carpeta.deleteRecursively()
    }

    private fun fotoEnDisco(nombre: String): String {
        val archivo = File(carpeta, nombre)
        archivo.outputStream().use {
            Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 80, it)
        }
        return archivo.absolutePath
    }

    private suspend fun diagnosticoCon(ruta: String): Diagnostico {
        val id = db.diagnosticoDao().insertar(
            Diagnostico(parcelaId = parcelaId, imagenRuta = ruta, resultado = "x", confianza = 0.5f)
        )
        return db.diagnosticoDao().obtenerPorId(id)!!
    }

    @Test
    fun una_foto_de_camara_completa_se_guarda_reducida() {
        // Del tamaño de las fotos de galería del teléfono de pruebas
        val grande = Bitmap.createBitmap(4160, 3120, Bitmap.Config.ARGB_8888)

        val ruta = ImagenStorage.guardar(context, ImagenStorage.reducir(grande))

        try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, limites)
            assertEquals(ImagenStorage.LADO_MAXIMO, maxOf(limites.outWidth, limites.outHeight))
            assertEquals(4160f / 3120f, limites.outWidth.toFloat() / limites.outHeight, 0.01f)
        } finally {
            File(ruta).delete()
        }
    }

    @Test
    fun una_foto_vieja_enorme_se_carga_reducida() {
        val archivo = File(carpeta, "vieja.jpg")
        archivo.outputStream().use {
            Bitmap.createBitmap(4160, 3120, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 80, it)
        }

        val miniatura = ImagenStorage.cargarDesdeArchivo(archivo.absolutePath, ImagenStorage.LADO_MINIATURA)!!

        assertEquals(ImagenStorage.LADO_MINIATURA, maxOf(miniatura.width, miniatura.height))
    }

    @Test
    fun borrar_un_diagnostico_borra_su_foto() = runBlocking {
        val ruta = fotoEnDisco("a.jpg")
        val diagnostico = diagnosticoCon(ruta)

        DiagnosticoRepository(db.diagnosticoDao()).eliminarDiagnostico(diagnostico)

        assertFalse(File(ruta).exists())
    }

    @Test
    fun cambiar_la_foto_al_editar_borra_la_anterior() = runBlocking {
        val vieja = fotoEnDisco("vieja.jpg")
        val nueva = fotoEnDisco("nueva.jpg")
        val diagnostico = diagnosticoCon(vieja)

        DiagnosticoRepository(db.diagnosticoDao())
            .actualizarDiagnostico(diagnostico.copy(imagenRuta = nueva), rutaImagenAnterior = vieja)

        assertFalse(File(vieja).exists())
        assertTrue(File(nueva).exists())
    }

    @Test
    fun editar_sin_cambiar_la_foto_no_la_borra() = runBlocking {
        val ruta = fotoEnDisco("misma.jpg")
        val diagnostico = diagnosticoCon(ruta)

        DiagnosticoRepository(db.diagnosticoDao())
            .actualizarDiagnostico(diagnostico.copy(resultado = "y"), rutaImagenAnterior = ruta)

        assertTrue(File(ruta).exists())
    }

    @Test
    fun borrar_una_parcela_borra_las_fotos_de_sus_diagnosticos() = runBlocking {
        val r1 = fotoEnDisco("1.jpg")
        val r2 = fotoEnDisco("2.jpg")
        diagnosticoCon(r1)
        diagnosticoCon(r2)
        val repo = ParcelaRepository(db.parcelaDao())

        repo.eliminarParcela(db.parcelaDao().obtenerPorId(parcelaId)!!)

        assertFalse(File(r1).exists())
        assertFalse(File(r2).exists())
        assertEquals(0, db.diagnosticoDao().rutasImagenes().size)
    }

    @Test
    fun el_aviso_cuenta_lo_que_se_borra_con_la_parcela() = runBlocking {
        diagnosticoCon(fotoEnDisco("1.jpg"))
        db.actividadDao().insertar(Actividad(parcelaId = parcelaId, tipo = "Riego", descripcion = "2 h"))
        db.actividadDao().insertar(Actividad(parcelaId = parcelaId, tipo = "Cosecha", descripcion = "10 kg"))

        val contenido = ParcelaRepository(db.parcelaDao()).contarContenido(parcelaId)

        assertEquals(1, contenido.diagnosticos)
        assertEquals(2, contenido.actividades)
    }

    @Test
    fun la_limpieza_borra_solo_las_fotos_que_ningun_diagnostico_usa() = runBlocking {
        val enUso = fotoEnDisco("en-uso.jpg")
        val huerfana1 = fotoEnDisco("huerfana-1.jpg")
        val huerfana2 = fotoEnDisco("huerfana-2.jpg")
        diagnosticoCon(enUso)

        val borradas = DiagnosticoRepository(db.diagnosticoDao()).limpiarImagenesHuerfanas(carpeta)

        assertEquals(2, borradas)
        assertTrue(File(enUso).exists())
        assertFalse(File(huerfana1).exists())
        assertFalse(File(huerfana2).exists())
    }
}
