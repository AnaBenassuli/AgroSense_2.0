package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.UsuarioDao
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ResultadoRegistro {
    data class Registrado(val usuario: Usuario) : ResultadoRegistro
    data object CorreoEnUso : ResultadoRegistro
}

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    fun observarUsuarios() =
        usuarioDao.observarUsuarios()

    suspend fun registrarUsuario(
        usuario: Usuario
    ): Long {
        return usuarioDao.insertar(usuario)
    }

    suspend fun actualizarUsuario(
        usuario: Usuario
    ) {
        usuarioDao.actualizar(usuario)
    }

    suspend fun eliminarUsuario(
        usuario: Usuario
    ) {
        usuarioDao.eliminar(usuario)
    }

    suspend fun obtenerPorId(
        id: Long
    ): Usuario? {
        return usuarioDao.obtenerPorId(id)
    }

    suspend fun obtenerPorCorreo(
        correo: String
    ): Usuario? {
        return usuarioDao.obtenerPorCorreo(correo)
    }

    suspend fun registrarConContrasena(
        nombre: String,
        correo: String,
        contrasena: String,
        tipoUsuario: String
    ): ResultadoRegistro {

        val correoNormalizado = normalizarCorreo(correo)
        val existente = usuarioDao.obtenerPorCorreo(correoNormalizado)

        if (existente != null && existente.passwordHash.isNotEmpty()) {
            return ResultadoRegistro.CorreoEnUso
        }

        val hash = withContext(Dispatchers.Default) {
            PasswordHasher.hash(contrasena)
        }

        // Una cuenta sin contraseña (creada antes de la v2 o desde Perfil) se completa en vez de duplicarse
        if (existente != null) {
            val completado = existente.copy(
                nombre = nombre,
                tipoUsuario = tipoUsuario,
                passwordHash = hash
            )
            usuarioDao.actualizar(completado)
            return ResultadoRegistro.Registrado(completado)
        }

        val nuevo = Usuario(
            nombre = nombre,
            correo = correoNormalizado,
            tipoUsuario = tipoUsuario,
            passwordHash = hash
        )
        val id = usuarioDao.insertar(nuevo)
        return ResultadoRegistro.Registrado(nuevo.copy(id = id))
    }

    /** Devuelve el usuario si el correo existe y la contraseña coincide; null en cualquier otro caso. */
    suspend fun autenticar(
        correo: String,
        contrasena: String
    ): Usuario? {

        val usuario = usuarioDao.obtenerPorCorreo(normalizarCorreo(correo))
            ?: return null

        if (usuario.passwordHash.isEmpty()) return null

        val valida = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(contrasena, usuario.passwordHash)
        }

        return if (valida) usuario else null
    }

    private fun normalizarCorreo(correo: String) =
        correo.trim().lowercase()
}
