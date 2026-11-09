package com.agrosense.app.data.repository

import com.agrosense.app.data.local.ImagenStorage
import com.agrosense.app.data.local.dao.DiagnosticoDao
import com.agrosense.app.data.local.entity.Diagnostico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DiagnosticoRepository(
    private val diagnosticoDao: DiagnosticoDao
) {

    suspend fun registrarDiagnostico(
        diagnostico: Diagnostico
    ): Long {
        return diagnosticoDao.insertar(diagnostico)
    }

    /** Si se cambió la foto, borra del disco la anterior para que no quede huérfana. */
    suspend fun actualizarDiagnostico(
        diagnostico: Diagnostico,
        rutaImagenAnterior: String? = null
    ) {
        diagnosticoDao.actualizar(diagnostico)

        if (rutaImagenAnterior != null && rutaImagenAnterior != diagnostico.imagenRuta) {
            withContext(Dispatchers.IO) { ImagenStorage.borrar(rutaImagenAnterior) }
        }
    }

    /** Borra el diagnóstico y su foto. */
    suspend fun eliminarDiagnostico(
        diagnostico: Diagnostico
    ) {
        diagnosticoDao.eliminar(diagnostico)
        withContext(Dispatchers.IO) { ImagenStorage.borrar(diagnostico.imagenRuta) }
    }

    /** Borra de [carpeta] las fotos que ningún diagnóstico usa. Devuelve cuántas borró. */
    suspend fun limpiarImagenesHuerfanas(
        carpeta: File
    ): Int {
        val enUso = diagnosticoDao.rutasImagenes()
        return withContext(Dispatchers.IO) { ImagenStorage.borrarHuerfanas(carpeta, enUso) }
    }

    fun observarPorParcela(
        parcelaId: Long
    ) = diagnosticoDao.observarPorParcela(parcelaId)

    fun observarTotalPorUsuario(
        usuarioId: Long
    ) = diagnosticoDao.observarTotalPorUsuario(usuarioId)

    suspend fun obtenerUltimo(
        parcelaId: Long
    ) = diagnosticoDao.obtenerUltimo(parcelaId)

    suspend fun obtenerPorId(
        id: Long
    ) = diagnosticoDao.obtenerPorId(id)
}