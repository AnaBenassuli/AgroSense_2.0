package com.agrosense.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agrosense.app.data.local.entity.Actividad
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(actividad: Actividad): Long

    @Update
    suspend fun actualizar(actividad: Actividad)

    @Delete
    suspend fun eliminar(actividad: Actividad)

    @Query(
        """
        SELECT * FROM actividades
        WHERE parcelaId = :parcelaId
        ORDER BY fecha DESC
        """
    )
    fun observarPorParcela(
        parcelaId: Long
    ): Flow<List<Actividad>>

    @Query(
        """
        SELECT * FROM actividades
        WHERE parcelaId = :parcelaId
        AND fecha BETWEEN :desde AND :hasta
        ORDER BY fecha DESC
        """
    )
    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Actividad>

    @Query(
        "SELECT * FROM actividades WHERE id = :id LIMIT 1"
    )
    suspend fun obtenerPorId(
        id: Long
    ): Actividad?
}