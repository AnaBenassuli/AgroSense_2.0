package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.SensorDao
import com.agrosense.app.data.local.entity.Sensor
import kotlinx.coroutines.flow.Flow

class SensorRepository(
    private val sensorDao: SensorDao
) {

    suspend fun registrarSensor(sensor: Sensor): Long {
        return sensorDao.insertar(sensor)
    }

    fun observarPorParcela(parcelaId: Long): Flow<List<Sensor>> {
        return sensorDao.observarPorParcela(parcelaId)
    }

    suspend fun obtenerUltimaLectura(parcelaId: Long): Sensor? {
        return sensorDao.obtenerUltimaLectura(parcelaId)
    }

    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Sensor> {
        return sensorDao.obtenerPorRango(
            parcelaId = parcelaId,
            desde = desde,
            hasta = hasta
        )
    }

    suspend fun eliminarPorParcela(parcelaId: Long) {
        sensorDao.eliminarPorParcela(parcelaId)
    }
}