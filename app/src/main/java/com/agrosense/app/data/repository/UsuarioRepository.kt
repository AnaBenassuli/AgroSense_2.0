package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.UsuarioDao
import com.agrosense.app.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    fun observarUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.observarTodos()
    }

    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        tipoUsuario: String = "Productor"
    ): Long {

        val usuario = Usuario(
            nombre = nombre.trim(),
            correo = correo.trim().lowercase(),
            tipoUsuario = tipoUsuario
        )

        return usuarioDao.insertar(usuario)
    }

    suspend fun obtenerPorCorreo(correo: String): Usuario? {
        return usuarioDao.obtenerPorCorreo(
            correo.trim().lowercase()
        )
    }

    suspend fun obtenerPorId(id: Long): Usuario? {
        return usuarioDao.obtenerPorId(id)
    }

    suspend fun actualizarUsuario(usuario: Usuario) {
        usuarioDao.actualizar(usuario)
    }
}