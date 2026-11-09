package com.agrosense.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agrosense.app.data.local.entity.Parcela
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(parcela: Parcela): Long

    @Update
    suspend fun actualizar(parcela: Parcela)

    @Delete
    suspend fun eliminar(parcela: Parcela)

    @Query("SELECT * FROM parcelas WHERE usuarioId = :usuarioId ORDER BY nombre")
    fun observarPorUsuario(usuarioId: Long): Flow<List<Parcela>>

    @Query("SELECT * FROM parcelas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): Parcela?

    // Lo que se borra en cascada con la parcela, para avisarlo antes y borrar las fotos del disco

    @Query("SELECT COUNT(*) FROM diagnosticos WHERE parcelaId = :parcelaId")
    suspend fun contarDiagnosticos(parcelaId: Long): Int

    @Query("SELECT COUNT(*) FROM actividades WHERE parcelaId = :parcelaId")
    suspend fun contarActividades(parcelaId: Long): Int

    @Query("SELECT imagenRuta FROM diagnosticos WHERE parcelaId = :parcelaId")
    suspend fun rutasImagenes(parcelaId: Long): List<String>
}