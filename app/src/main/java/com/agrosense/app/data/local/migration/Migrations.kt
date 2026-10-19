package com.agrosense.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: agrega la contraseña (hash) a los usuarios.
 *
 * Los usuarios existentes quedan con hash vacío; no pueden iniciar sesión hasta que se registren
 * otra vez con el mismo correo, lo que les asigna contraseña sin perder el registro.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE usuarios ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''"
        )
    }
}

val TODAS_LAS_MIGRACIONES = arrayOf(
    MIGRATION_1_2
)
