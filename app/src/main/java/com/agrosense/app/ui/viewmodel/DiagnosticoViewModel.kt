package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.repository.DiagnosticoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiagnosticoViewModel(
    private val repository: DiagnosticoRepository
) : ViewModel() {

    val totalDiagnosticos: StateFlow<Int> =
        repository.observarTotal()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

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