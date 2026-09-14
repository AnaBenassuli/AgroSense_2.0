package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.UsuarioDao
import com.agrosense.app.data.local.entity.Usuario

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
}