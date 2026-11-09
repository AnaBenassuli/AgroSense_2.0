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

/**
 * v2 -> v3: cada diagnóstico dice a qué módulo pertenece (cultivo o ganado).
 *
 * Hasta ahora solo existía el de cultivos, así que todas las filas existentes quedan como CULTIVO.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE diagnosticos ADD COLUMN modulo TEXT NOT NULL DEFAULT 'CULTIVO'"
        )
    }
}

/**
 * v3 -> v4: cada parcela tiene dueño (usuarioId). Antes todas las cuentas veían las parcelas,
 * diagnósticos y actividades de todas.
 *
 * Las parcelas que ya existían no tienen forma de saber quién las creó. Se asignan a la primera
 * cuenta con contraseña (la que puede iniciar sesión), o a la primera cuenta si ninguna tiene;
 * si no hay usuarios, quedan sin dueño.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parcelas ADD COLUMN usuarioId INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_parcelas_usuarioId ON parcelas (usuarioId)")
        db.execSQL(
            """
            UPDATE parcelas SET usuarioId = (
                SELECT id FROM usuarios ORDER BY (passwordHash = '') ASC, id ASC LIMIT 1
            )
            """
        )
    }
}

val TODAS_LAS_MIGRACIONES = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4
)
