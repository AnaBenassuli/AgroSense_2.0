package com.agrosense.app.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash de contraseñas para el acceso local (antes lo resolvía Firebase Auth).
 *
 * Usa PBKDF2-HMAC-SHA512 con sal aleatoria, que es lo que recomienda OWASP. El resultado se
 * guarda como una sola cadena `pbkdf2_sha512$iteraciones$sal$hash`, así que se pueden subir
 * las iteraciones más adelante sin invalidar las contraseñas ya guardadas.
 *
 * Es costoso a propósito (cientos de ms en un teléfono): llamarlo fuera del hilo principal.
 */
object PasswordHasher {

    const val ITERACIONES = 210_000

    private const val ALGORITMO = "pbkdf2_sha512"
    private const val ALGORITMO_JCA = "PBKDF2WithHmacSHA512"
    private const val BYTES_SAL = 16
    private const val BITS_HASH = 512

    private val random = SecureRandom()

    fun hash(contrasena: String): String {
        val sal = ByteArray(BYTES_SAL).also { random.nextBytes(it) }
        val hash = derivar(contrasena, sal, ITERACIONES)
        val b64 = Base64.getEncoder()

        return "$ALGORITMO$$ITERACIONES$${b64.encodeToString(sal)}$${b64.encodeToString(hash)}"
    }

    fun verificar(contrasena: String, almacenado: String): Boolean {
        val partes = almacenado.split("$")
        if (partes.size != 4 || partes[0] != ALGORITMO) return false

        val iteraciones = partes[1].toIntOrNull() ?: return false
        if (iteraciones <= 0) return false

        val (sal, esperado) = try {
            val b64 = Base64.getDecoder()
            b64.decode(partes[2]) to b64.decode(partes[3])
        } catch (e: IllegalArgumentException) {
            return false
        }
        if (sal.isEmpty() || esperado.isEmpty()) return false

        // Comparación en tiempo constante para no filtrar información por tiempos de respuesta
        return MessageDigest.isEqual(derivar(contrasena, sal, iteraciones), esperado)
    }

    private fun derivar(contrasena: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO_JCA).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
