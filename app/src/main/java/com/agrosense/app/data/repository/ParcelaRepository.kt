package com.agrosense.app.data.repository

import com.agrosense.app.data.local.ImagenStorage
import com.agrosense.app.data.local.dao.ParcelaDao
import com.agrosense.app.data.local.entity.Parcela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ParcelaRepository(
    private val parcelaDao: ParcelaDao
) {

    fun observarPorUsuario(usuarioId: Long): Flow<List<Parcela>> {
        return parcelaDao.observarPorUsuario(usuarioId)
    }

    suspend fun registrarParcela(
        usuarioId: Long,
        nombre: String,
        ubicacion: String,
        cultivo: String
    ): Long {

        val parcela = Parcela(
            nombre = nombre.trim(),
            ubicacion = ubicacion.trim(),
            cultivo = cultivo.trim(),
            usuarioId = usuarioId
        )

        return parcelaDao.insertar(parcela)
    }

    suspend fun obtenerPorId(id: Long): Parcela? {
        return parcelaDao.obtenerPorId(id)
    }

    suspend fun actualizarParcela(parcela: Parcela) {
        parcelaDao.actualizar(parcela)
    }

    /**
     * Borra la parcela. Room borra en cascada sus diagnósticos, actividades y sensores; las fotos
     * de los diagnósticos están en disco y se borran aparte.
     */
    suspend fun eliminarParcela(parcela: Parcela) {
        val fotos = parcelaDao.rutasImagenes(parcela.id)
        parcelaDao.eliminar(parcela)
        withContext(Dispatchers.IO) { fotos.forEach(ImagenStorage::borrar) }
    }

    /** Cuántos diagnósticos y actividades se borrarían junto con la parcela. */
    suspend fun contarContenido(parcelaId: Long): ContenidoParcela =
        ContenidoParcela(
            diagnosticos = parcelaDao.contarDiagnosticos(parcelaId),
            actividades = parcelaDao.contarActividades(parcelaId)
        )
}

data class ContenidoParcela(
    val diagnosticos: Int,
    val actividades: Int
)