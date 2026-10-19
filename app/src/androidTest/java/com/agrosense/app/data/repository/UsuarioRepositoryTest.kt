package com.agrosense.app.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.AgroSenseDatabase
import com.agrosense.app.data.local.entity.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioRepositoryTest {

    private lateinit var db: AgroSenseDatabase
    private lateinit var repository: UsuarioRepository

    @Before
    fun crearBase() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AgroSenseDatabase::class.java
        ).build()
        repository = UsuarioRepository(db.usuarioDao())
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    @Test
    fun registrar_y_luego_iniciar_sesion_con_la_misma_contrasena() = runBlocking {
        val registro = repository.registrarConContrasena("Luis", "luis@agrosense.mx", "cosecha2026", "Productor")

        assertTrue(registro is ResultadoRegistro.Registrado)
        assertNotNull(repository.autenticar("luis@agrosense.mx", "cosecha2026"))
    }

    @Test
    fun la_contrasena_no_se_guarda_en_texto_plano() = runBlocking {
        repository.registrarConContrasena("Luis", "luis@agrosense.mx", "cosecha2026", "Productor")

        val guardado = db.usuarioDao().obtenerPorCorreo("luis@agrosense.mx")!!
        assertTrue(guardado.passwordHash.isNotEmpty())
        assertTrue(!guardado.passwordHash.contains("cosecha2026"))
    }

    @Test
    fun contrasena_incorrecta_o_correo_inexistente_no_autentican() = runBlocking {
        repository.registrarConContrasena("Luis", "luis@agrosense.mx", "cosecha2026", "Productor")

        assertNull(repository.autenticar("luis@agrosense.mx", "otra-contrasena"))
        assertNull(repository.autenticar("nadie@agrosense.mx", "cosecha2026"))
    }

    @Test
    fun el_correo_no_distingue_mayusculas_ni_espacios() = runBlocking {
        repository.registrarConContrasena("Luis", "  Luis@AgroSense.mx ", "cosecha2026", "Productor")

        assertNotNull(repository.autenticar("LUIS@agrosense.mx", "cosecha2026"))
    }

    @Test
    fun no_se_puede_registrar_dos_veces_el_mismo_correo() = runBlocking {
        repository.registrarConContrasena("Luis", "luis@agrosense.mx", "cosecha2026", "Productor")

        val segundo = repository.registrarConContrasena("Otro", "luis@agrosense.mx", "otra-contrasena", "Agrónomo")

        assertEquals(ResultadoRegistro.CorreoEnUso, segundo)
        assertNotNull(repository.autenticar("luis@agrosense.mx", "cosecha2026"))
    }

    @Test
    fun una_cuenta_sin_contrasena_no_inicia_sesion_y_se_completa_al_registrarse() = runBlocking {
        val id = db.usuarioDao().insertar(Usuario(nombre = "Ana", correo = "ana@agrosense.mx"))

        assertNull(repository.autenticar("ana@agrosense.mx", ""))

        val registro = repository.registrarConContrasena("Ana", "ana@agrosense.mx", "cosecha2026", "Productor")

        assertEquals(id, (registro as ResultadoRegistro.Registrado).usuario.id)
        assertNotNull(repository.autenticar("ana@agrosense.mx", "cosecha2026"))
    }
}
