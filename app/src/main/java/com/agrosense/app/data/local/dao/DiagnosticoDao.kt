package com.agrosense.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agrosense.app.data.local.entity.Diagnostico
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(diagnostico: Diagnostico): Long

    @Update
    suspend fun actualizar(diagnostico: Diagnostico)

    @Delete
    suspend fun eliminar(diagnostico: Diagnostico)

    @Query(
        """
        SELECT * FROM diagnosticos
        WHERE parcelaId = :parcelaId
        ORDER BY fecha DESC
        """
    )
    fun observarPorParcela(
        parcelaId: Long
    ): Flow<List<Diagnostico>>

    @Query(
        """
        SELECT * FROM diagnosticos
        WHERE parcelaId = :parcelaId
        ORDER BY fecha DESC
        LIMIT 1
        """
    )
    suspend fun obtenerUltimo(
        parcelaId: Long
    ): Diagnostico?

    @Query(
        """
        SELECT COUNT(*) FROM diagnosticos d
        INNER JOIN parcelas p ON p.id = d.parcelaId
        WHERE p.usuarioId = :usuarioId
        """
    )
    fun observarTotalPorUsuario(usuarioId: Long): Flow<Int>

    @Query(
        "SELECT * FROM diagnosticos WHERE id = :id LIMIT 1"
    )
    suspend fun obtenerPorId(
        id: Long
    ): Diagnostico?
}