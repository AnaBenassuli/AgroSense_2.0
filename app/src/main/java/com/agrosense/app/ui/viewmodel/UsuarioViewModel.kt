package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    val usuarios: StateFlow<List<Usuario>> =
        repository.observarUsuarios()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun registrarUsuario(
        nombre: String,
        correo: String,
        tipoUsuario: String
    ) {

        viewModelScope.launch {

            repository.registrarUsuario(
                Usuario(
                    nombre = nombre,
                    correo = correo,
                    tipoUsuario = tipoUsuario
                )
            )
        }
    }

    fun actualizarUsuario(
        usuario: Usuario
    ) {

        viewModelScope.launch {
            repository.actualizarUsuario(usuario)
        }
    }

    fun eliminarUsuario(
        usuario: Usuario
    ) {

        viewModelScope.launch {
            repository.eliminarUsuario(usuario)
        }
    }
}