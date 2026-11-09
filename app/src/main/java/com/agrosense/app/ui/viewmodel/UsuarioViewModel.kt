package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Usuario
import com.agrosense.app.data.repository.ResultadoRegistro
import com.agrosense.app.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoAcceso {
    data object Inactivo : EstadoAcceso
    data object Procesando : EstadoAcceso
    data class Error(val mensaje: String) : EstadoAcceso
    data class Autenticado(val usuario: Usuario) : EstadoAcceso
}

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

    private val _estadoAcceso =
        MutableStateFlow<EstadoAcceso>(EstadoAcceso.Inactivo)

    val estadoAcceso: StateFlow<EstadoAcceso> =
        _estadoAcceso.asStateFlow()

    fun iniciarSesion(
        correo: String,
        contrasena: String
    ) {

        if (_estadoAcceso.value is EstadoAcceso.Procesando) return
        _estadoAcceso.value = EstadoAcceso.Procesando

        viewModelScope.launch {

            val usuario = repository.autenticar(correo, contrasena)

            // Mismo mensaje si el correo no existe o la contraseña falla, para no revelar qué cuentas hay
            _estadoAcceso.value =
                if (usuario != null) {
                    EstadoAcceso.Autenticado(usuario)
                } else {
                    EstadoAcceso.Error("Correo o contraseña incorrectos.")
                }
        }
    }

    fun registrarCuenta(
        nombre: String,
        correo: String,
        contrasena: String,
        tipoUsuario: String
    ) {

        if (_estadoAcceso.value is EstadoAcceso.Procesando) return
        _estadoAcceso.value = EstadoAcceso.Procesando

        viewModelScope.launch {

            _estadoAcceso.value =
                when (
                    val resultado = repository.registrarConContrasena(
                        nombre = nombre,
                        correo = correo,
                        contrasena = contrasena,
                        tipoUsuario = tipoUsuario
                    )
                ) {
                    is ResultadoRegistro.Registrado ->
                        EstadoAcceso.Autenticado(resultado.usuario)

                    ResultadoRegistro.CorreoEnUso ->
                        EstadoAcceso.Error("Ya existe una cuenta con ese correo.")
                }
        }
    }

    fun cerrarSesion() {
        _estadoAcceso.value = EstadoAcceso.Inactivo
    }

    fun limpiarErrorAcceso() {
        if (_estadoAcceso.value is EstadoAcceso.Error) {
            _estadoAcceso.value = EstadoAcceso.Inactivo
        }
    }

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