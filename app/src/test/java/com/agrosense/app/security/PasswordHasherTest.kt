package com.agrosense.app.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `la contrasena correcta se verifica`() {
        val hash = PasswordHasher.hash("cosecha2026")

        assertTrue(PasswordHasher.verificar("cosecha2026", hash))
    }

    @Test
    fun `una contrasena distinta no se verifica`() {
        val hash = PasswordHasher.hash("cosecha2026")

        assertFalse(PasswordHasher.verificar("cosecha2027", hash))
        assertFalse(PasswordHasher.verificar("", hash))
    }

    @Test
    fun `el hash no contiene la contrasena en texto plano`() {
        val hash = PasswordHasher.hash("cosecha2026")

        assertFalse(hash.contains("cosecha2026"))
    }

    @Test
    fun `la misma contrasena produce hashes distintos por la sal`() {
        assertNotEquals(
            PasswordHasher.hash("cosecha2026"),
            PasswordHasher.hash("cosecha2026")
        )
    }

    @Test
    fun `el hash guarda algoritmo e iteraciones para poder subirlas despues`() {
        val partes = PasswordHasher.hash("cosecha2026").split("$")

        assertEquals(4, partes.size)
        assertEquals("pbkdf2_sha512", partes[0])
        assertEquals(PasswordHasher.ITERACIONES.toString(), partes[1])
    }

    @Test
    fun `un hash vacio o mal formado nunca se verifica`() {
        assertFalse(PasswordHasher.verificar("cosecha2026", ""))
        assertFalse(PasswordHasher.verificar("cosecha2026", "texto-plano"))
        assertFalse(PasswordHasher.verificar("cosecha2026", "pbkdf2_sha512\$abc\$x\$y"))
        assertFalse(PasswordHasher.verificar("cosecha2026", "md5\$1\$AAAA\$AAAA"))
    }
}
