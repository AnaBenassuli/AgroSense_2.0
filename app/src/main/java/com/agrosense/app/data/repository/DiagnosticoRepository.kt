package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.DiagnosticoDao
import com.agrosense.app.data.local.entity.Diagnostico

class DiagnosticoRepository(
    private val diagnosticoDao: DiagnosticoDao
) {

    suspend fun registrarDiagnostico(
        diagnostico: Diagnostico
    ): Long {
        return diagnosticoDao.insertar(diagnostico)
    }

    suspend fun actualizarDiagnostico(
        diagnostico: Diagnostico
    ) {
        diagnosticoDao.actualizar(diagnostico)
    }

    suspend fun eliminarDiagnostico(
        diagnostico: Diagnostico
    ) {
        diagnosticoDao.eliminar(diagnostico)
    }

    fun observarPorParcela(
        parcelaId: Long
    ) = diagnosticoDao.observarPorParcela(parcelaId)

    suspend fun obtenerUltimo(
        parcelaId: Long
    ) = diagnosticoDao.obtenerUltimo(parcelaId)

    suspend fun obtenerPorId(
        id: Long
    ) = diagnosticoDao.obtenerPorId(id)
}