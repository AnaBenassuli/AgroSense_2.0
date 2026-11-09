package com.agrosense.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lecturas de humedad y temperatura de una parcela. Ninguna pantalla las usa todavía (se pensó
 * para sensores en campo); la tabla se queda porque ya existe en las bases instaladas y quitarla
 * exigiría una migración de Room. El ViewModel y el repositorio sin uso se borraron.
 */
@Entity(
    tableName = "sensores",
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
data class Sensor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parcelaId: Long,
    val humedad: Float,
    val temperatura: Float,
    val fechaHora: Long = System.currentTimeMillis()
)