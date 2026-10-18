package com.agrosense.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val tipoUsuario: String = "Productor",
    // Salida de PasswordHasher; vacío = cuenta sin contraseña (creada antes de la v2 o desde Perfil)
    @ColumnInfo(defaultValue = "")
    val passwordHash: String = ""
)
