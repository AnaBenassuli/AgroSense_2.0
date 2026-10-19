package com.agrosense.app.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.migration.MIGRATION_1_2
import com.agrosense.app.data.local.migration.TODAS_LAS_MIGRACIONES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val nombreDb = "migracion-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AgroSenseDatabase::class.java
    )

    @Test
    fun migrar_1_a_2_conserva_los_usuarios_y_los_deja_sin_contrasena() {
        helper.createDatabase(nombreDb, 1).apply {
            execSQL(
                "INSERT INTO usuarios (id, nombre, correo, tipoUsuario) " +
                    "VALUES (1, 'Ana', 'ana@agrosense.mx', 'Productor')"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreDb, 2, true, MIGRATION_1_2)

        db.query("SELECT nombre, correo, passwordHash FROM usuarios WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Ana", cursor.getString(0))
            assertEquals("ana@agrosense.mx", cursor.getString(1))
            assertEquals("", cursor.getString(2))
        }
    }

    @Test
    fun room_abre_la_base_migrada_con_todas_las_migraciones() {
        helper.createDatabase(nombreDb, 1).close()

        // Falla si alguna versión no tiene migración registrada en DatabaseProvider
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AgroSenseDatabase::class.java,
            nombreDb
        )
            .addMigrations(*TODAS_LAS_MIGRACIONES)
            .build()
            .apply {
                openHelper.writableDatabase
                close()
            }
    }
}
