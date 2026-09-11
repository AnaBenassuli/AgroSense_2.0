package com.agrosense.app.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Acceso local del prototipo: el usuario sólo escribe su nombre, no hay
 * credenciales ni servidor. El nombre vive en SharedPreferences.
 *
 * "Recordarme" decide si la sesión sobrevive al cierre de la app:
 * llamar a [limpiarSesionNoRecordada] al arrancar borra el nombre cuando
 * el usuario no pidió que se recordara.
 */
class LocalAuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserName(name: String) {
        prefs.edit { putString(KEY_USER_NAME, name.trim()) }
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun isLoggedIn(): Boolean = !getUserName().isNullOrBlank()

    fun logout() {
        prefs.edit {
            remove(KEY_USER_NAME)
            remove(KEY_RECORDARME)
        }
    }

    /** Guarda el nombre y si la sesión debe recordarse entre arranques. */
    fun iniciarSesion(nombre: String, recordarme: Boolean) {
        prefs.edit {
            putString(KEY_USER_NAME, nombre.trim())
            putBoolean(KEY_RECORDARME, recordarme)
        }
    }

    fun getRecordarme(): Boolean = prefs.getBoolean(KEY_RECORDARME, false)

    /**
     * Borra la sesión si el usuario no marcó "Recordarme". Se llama una vez
     * al iniciar la app, antes de decidir la pantalla inicial.
     */
    fun limpiarSesionNoRecordada() {
        if (isLoggedIn() && !getRecordarme()) {
            logout()
        }
    }

    private companion object {
        const val PREFS_NAME = "agrosense"
        const val KEY_USER_NAME = "user_name"
        const val KEY_RECORDARME = "recordarme"
    }
}
