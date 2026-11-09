package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.ActividadDao
import com.agrosense.app.data.local.entity.Actividad

class ActividadRepository(
    private val actividadDao: ActividadDao
) {

    suspend fun registrarActividad(
        actividad: Actividad
    ): Long {
        return actividadDao.insertar(actividad)
    }

    suspend fun actualizarActividad(
        actividad: Actividad
    ) {
        actividadDao.actualizar(actividad)
    }

    suspend fun eliminarActividad(
        actividad: Actividad
    ) {
        actividadDao.eliminar(actividad)
    }

    fun observarPorParcela(
        parcelaId: Long
    ) = actividadDao.observarPorParcela(parcelaId)

    fun observarTotalPorUsuario(
        usuarioId: Long
    ) = actividadDao.observarTotalPorUsuario(usuarioId)

    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ) = actividadDao.obtenerPorRango(
        parcelaId,
        desde,
        hasta
    )

    suspend fun obtenerPorId(
        id: Long
    ) = actividadDao.obtenerPorId(id)
}