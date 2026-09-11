package com.agrosense.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agrosense.app.data.local.entity.Sensor
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sensor: Sensor): Long

    @Query("""
        SELECT * FROM sensores
        WHERE parcelaId = :parcelaId
        ORDER BY fechaHora DESC
    """)
    fun observarPorParcela(parcelaId: Long): Flow<List<Sensor>>

    @Query("""
        SELECT * FROM sensores
        WHERE parcelaId = :parcelaId
        ORDER BY fechaHora DESC
        LIMIT 1
    """)
    suspend fun obtenerUltimaLectura(parcelaId: Long): Sensor?

    @Query("""
        SELECT * FROM sensores
        WHERE parcelaId = :parcelaId
        AND fechaHora >= :desde
        AND fechaHora <= :hasta
        ORDER BY fechaHora ASC
    """)
    suspend fun obtenerPorRango(
        parcelaId: Long,
        desde: Long,
        hasta: Long
    ): List<Sensor>

    @Query("DELETE FROM sensores WHERE parcelaId = :parcelaId")
    suspend fun eliminarPorParcela(parcelaId: Long)
}