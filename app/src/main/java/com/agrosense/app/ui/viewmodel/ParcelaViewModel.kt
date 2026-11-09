package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.repository.ParcelaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ParcelaViewModel(
    private val repository: ParcelaRepository
) : ViewModel() {

    // Usuario con la sesión iniciada; null = nadie, y entonces no se muestra ninguna parcela
    private val usuarioId = MutableStateFlow<Long?>(null)

    val parcelas: StateFlow<List<Parcela>> =
        usuarioId
            .flatMapLatest { id ->
                if (id == null) flowOf(emptyList()) else repository.observarPorUsuario(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun establecerUsuario(id: Long?) {
        usuarioId.value = id
    }

    fun registrarParcela(
        nombre: String,
        ubicacion: String,
        cultivo: String
    ) {
        val dueno = usuarioId.value ?: return

        viewModelScope.launch {
            repository.registrarParcela(
                usuarioId = dueno,
                nombre = nombre,
                ubicacion = ubicacion,
                cultivo = cultivo
            )
        }
    }

    fun actualizarParcela(parcela: Parcela) {
        viewModelScope.launch {
            repository.actualizarParcela(parcela)
        }
    }

    fun eliminarParcela(parcela: Parcela) {
        viewModelScope.launch {
            repository.eliminarParcela(parcela)
        }
    }
}
