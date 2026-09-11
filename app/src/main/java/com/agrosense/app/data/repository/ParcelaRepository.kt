package com.agrosense.app.data.repository

import com.agrosense.app.data.local.dao.ParcelaDao
import com.agrosense.app.data.local.entity.Parcela
import kotlinx.coroutines.flow.Flow

class ParcelaRepository(
    private val parcelaDao: ParcelaDao
) {

    fun observarParcelas(): Flow<List<Parcela>> {
        return parcelaDao.observarTodas()
    }

    suspend fun registrarParcela(
        nombre: String,
        ubicacion: String,
        cultivo: String
    ): Long {

        val parcela = Parcela(
            nombre = nombre.trim(),
            ubicacion = ubicacion.trim(),
            cultivo = cultivo.trim()
        )

        return parcelaDao.insertar(parcela)
    }

    suspend fun obtenerPorId(id: Long): Parcela? {
        return parcelaDao.obtenerPorId(id)
    }

    suspend fun actualizarParcela(parcela: Parcela) {
        parcelaDao.actualizar(parcela)
    }

    suspend fun eliminarParcela(parcela: Parcela) {
        parcelaDao.eliminar(parcela)
    }
}