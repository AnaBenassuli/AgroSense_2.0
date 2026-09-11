package com.agrosense.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrosense.app.data.local.entity.Sensor
import com.agrosense.app.data.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SensorViewModel(
    private val repository: SensorRepository
) : ViewModel() {

    fun observarPorParcela(parcelaId: Long): Flow<List<Sensor>> {
        return repository.observarPorParcela(parcelaId)
    }

    fun registrarSensor(sensor: Sensor) {
        viewModelScope.launch {
            repository.registrarSensor(sensor)
        }
    }

    suspend fun obtenerUltimaLectura(parcelaId: Long): Sensor? {
        return repository.obtenerUltimaLectura(parcelaId)
    }

    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Sensor> {
        return repository.obtenerPorRango(
            parcelaId = parcelaId,
            desde = desde,
            hasta = hasta
        )
    }

    fun eliminarPorParcela(parcelaId: Long) {
        viewModelScope.launch {
            repository.eliminarPorParcela(parcelaId)
        }
    }
}