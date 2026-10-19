package com.agrosense.app.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.agrosense.app.data.local.migration.MIGRATION_1_2
import com.agrosense.app.data.local.migration.MIGRATION_2_3
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
    fun migrar_2_a_3_marca_los_diagnosticos_existentes_como_cultivo() {
        helper.createDatabase(nombreDb, 2).apply {
            insertarParcelaYDiagnostico()
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreDb, 3, true, MIGRATION_2_3)

        db.query("SELECT resultado, modulo FROM diagnosticos WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Tomato___Early_blight", cursor.getString(0))
            assertEquals("CULTIVO", cursor.getString(1))
        }
    }

    @Test
    fun migrar_1_a_3_de_una_vez_conserva_usuarios_y_diagnosticos() {
        helper.createDatabase(nombreDb, 1).apply {
            execSQL(
                "INSERT INTO usuarios (id, nombre, correo, tipoUsuario) " +
                    "VALUES (1, 'Ana', 'ana@agrosense.mx', 'Productor')"
            )
            insertarParcelaYDiagnostico()
            close()
        }

        val db = helper.runMigrationsAndValidate(nombreDb, 3, true, *TODAS_LAS_MIGRACIONES)

        db.query("SELECT passwordHash FROM usuarios WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
        }
        db.query("SELECT modulo FROM diagnosticos WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("CULTIVO", cursor.getString(0))
        }
    }

    // Columnas tal como existen en las versiones 1 y 2, sin el campo modulo
    private fun androidx.sqlite.db.SupportSQLiteDatabase.insertarParcelaYDiagnostico() {
        execSQL(
            "INSERT INTO parcelas (id, nombre, ubicacion, cultivo, fechaRegistro) " +
                "VALUES (1, 'Parcela norte', 'Monterrey', 'Tomate', 0)"
        )
        execSQL(
            "INSERT INTO diagnosticos (id, parcelaId, fecha, imagenRuta, resultado, confianza, top3, observaciones) " +
                "VALUES (1, 1, 0, '/foto.jpg', 'Tomato___Early_blight', 0.9, '', NULL)"
        )
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
