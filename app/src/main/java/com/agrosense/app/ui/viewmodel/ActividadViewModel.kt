package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.repository.ActividadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    private val usuarioId = MutableStateFlow<Long?>(null)

    /** Actividades del usuario con la sesión iniciada, en todas sus parcelas. */
    val totalActividades: StateFlow<Int> =
        usuarioId
            .flatMapLatest { id ->
                if (id == null) flowOf(0) else repository.observarTotalPorUsuario(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    fun establecerUsuario(id: Long?) {
        usuarioId.value = id
    }

    fun observarPorParcela(
        parcelaId: Long
    ): Flow<List<Actividad>> {
        return repository.observarPorParcela(parcelaId)
    }

    fun registrarActividad(
        parcelaId: Long,
        tipo: String,
        descripcion: String
    ) {
        viewModelScope.launch {

            repository.registrarActividad(
                Actividad(
                    parcelaId = parcelaId,
                    tipo = tipo,
                    descripcion = descripcion
                )
            )
        }
    }

    fun actualizarActividad(
        actividad: Actividad
    ) {
        viewModelScope.launch {
            repository.actualizarActividad(actividad)
        }
    }

    fun eliminarActividad(
        actividad: Actividad
    ) {
        viewModelScope.launch {
            repository.eliminarActividad(actividad)
        }
    }

    suspend fun obtenerPorId(
        id: Long
    ): Actividad? {
        return repository.obtenerPorId(id)
    }
}