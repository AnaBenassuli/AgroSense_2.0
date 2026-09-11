package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.ActividadDao
import com.agrosense.app.data.local.entity.Actividad
import kotlinx.coroutines.flow.Flow

class ActividadRepository(
    private val actividadDao: ActividadDao
) {

    suspend fun registrarActividad(actividad: Actividad): Long {
        return actividadDao.insertar(actividad)
    }

    suspend fun eliminarActividad(actividad: Actividad) {
        actividadDao.eliminar(actividad)
    }

    fun observarPorParcela(parcelaId: Long): Flow<List<Actividad>> {
        return actividadDao.observarPorParcela(parcelaId)
    }

    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Actividad> {
        return actividadDao.obtenerPorRango(
            parcelaId = parcelaId,
            desde = desde,
            hasta = hasta
        )
    }

    suspend fun obtenerPorId(id: Long): Actividad? {
        return actividadDao.obtenerPorId(id)
    }
}