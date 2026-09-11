package com.agrosense.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diagnosticos",
    foreignKeys = [
        ForeignKey(
            entity = Parcela::class,
            parentColumns = ["id"],
            childColumns = ["parcelaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parcelaId")]
)
data class Diagnostico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parcelaId: Long,
    val fecha: Long = System.currentTimeMillis(),
    val imagenRuta: String,
    val resultado: String,
    val confianza: Float,
    val top3: List<Prediccion> = emptyList(),
    val observaciones: String? = null
)