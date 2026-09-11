package com.agrosense.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parcelas")
data class Parcela(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val ubicacion: String,
    val cultivo: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)