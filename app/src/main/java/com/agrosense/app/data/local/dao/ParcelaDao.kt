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
}