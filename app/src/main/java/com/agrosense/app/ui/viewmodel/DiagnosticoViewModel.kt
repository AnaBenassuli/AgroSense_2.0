package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.repository.DiagnosticoRepository
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
class DiagnosticoViewModel(
    private val repository: DiagnosticoRepository
) : ViewModel() {

    private val usuarioId = MutableStateFlow<Long?>(null)

    /** Diagnósticos del usuario con la sesión iniciada, en todas sus parcelas. */
    val totalDiagnosticos: StateFlow<Int> =
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
    ): Flow<List<Diagnostico>> {
        return repository.observarPorParcela(parcelaId)
    }

    fun registrarDiagnostico(
        diagnostico: Diagnostico
    ) {
        viewModelScope.launch {
            repository.registrarDiagnostico(diagnostico)
        }
    }

    fun actualizarDiagnostico(
        diagnostico: Diagnostico
    ) {
        viewModelScope.launch {
            repository.actualizarDiagnostico(diagnostico)
        }
    }

    fun eliminarDiagnostico(
        diagnostico: Diagnostico
    ) {
        viewModelScope.launch {
            repository.eliminarDiagnostico(diagnostico)
        }
    }

    suspend fun obtenerUltimo(
        parcelaId: Long
    ): Diagnostico? {
        return repository.obtenerUltimo(parcelaId)
    }

    suspend fun obtenerPorId(
        id: Long
    ): Diagnostico? {
        return repository.obtenerPorId(id)
    }
}