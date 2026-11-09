package com.agrosense.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parcelas",
    indices = [Index("usuarioId")]
)
data class Parcela(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val ubicacion: String,
    val cultivo: String,
    val fechaRegistro: Long = System.currentTimeMillis(),
    // Dueño de la parcela. Diagnósticos, actividades y sensores cuelgan de la parcela, así que con
    // esto cada cuenta ve solo sus datos. Null = parcela sin dueño (ninguna cuenta la ve).
    val usuarioId: Long? = null
)
