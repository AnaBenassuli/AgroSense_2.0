package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.DiagnosticoDao
import com.agrosense.app.data.local.entity.Diagnostico
import kotlinx.coroutines.flow.Flow

class DiagnosticoRepository(
    private val diagnosticoDao: DiagnosticoDao
) {

    suspend fun registrarDiagnostico(diagnostico: Diagnostico): Long {
        return diagnosticoDao.insertar(diagnostico)
    }

    suspend fun eliminarDiagnostico(diagnostico: Diagnostico) {
        diagnosticoDao.eliminar(diagnostico)
    }

    fun observarPorParcela(parcelaId: Long): Flow<List<Diagnostico>> {
        return diagnosticoDao.observarPorParcela(parcelaId)
    }

    suspend fun obtenerUltimo(parcelaId: Long): Diagnostico? {
        return diagnosticoDao.obtenerUltimo(parcelaId)
    }

    suspend fun obtenerPorId(id: Long): Diagnostico? {
        return diagnosticoDao.obtenerPorId(id)
    }
}