package com.agrosense.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividades",
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
data class Actividad(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parcelaId: Long,
    val tipo: String,
    val fecha: Long = System.currentTimeMillis(),
    val descripcion: String
)