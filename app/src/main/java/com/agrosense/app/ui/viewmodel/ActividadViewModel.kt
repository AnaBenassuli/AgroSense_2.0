package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.repository.ActividadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ActividadViewModel(
    private val repository: ActividadRepository
) : ViewModel() {

    fun observarPorParcela(parcelaId: Long): Flow<List<Actividad>> {
        return repository.observarPorParcela(parcelaId)
    }

    fun registrarActividad(actividad: Actividad) {
        viewModelScope.launch {
            repository.registrarActividad(actividad)
        }
    }

    fun eliminarActividad(actividad: Actividad) {
        viewModelScope.launch {
            repository.eliminarActividad(actividad)
        }
    }

    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Actividad> {
        return repository.obtenerPorRango(
            parcelaId = parcelaId,
            desde = desde,
            hasta = hasta
        )
    }

    suspend fun obtenerPorId(id: Long): Actividad? {
        return repository.obtenerPorId(id)
    }
}