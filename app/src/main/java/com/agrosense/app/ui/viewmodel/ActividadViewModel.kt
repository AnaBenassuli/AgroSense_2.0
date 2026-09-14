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