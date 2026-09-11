package com.agrosense.app.di

import android.content.Context
import com.agrosense.app.data.local.DatabaseProvider
import com.agrosense.app.data.repository.ActividadRepository
import com.agrosense.app.data.repository.DiagnosticoRepository
import com.agrosense.app.data.repository.ParcelaRepository
import com.agrosense.app.data.repository.SensorRepository
import com.agrosense.app.data.repository.UsuarioRepository

/**
 * Construcción de dependencias del prototipo. No es un framework de
 * inyección: es el punto único donde se arma la cadena
 * Context -> Room -> DAO -> Repositorio, para que ninguna pantalla
 * tenga que conocerla.
 */
object AppModule {

    fun parcelaRepository(context: Context) =
        ParcelaRepository(DatabaseProvider.getDatabase(context).parcelaDao())

    fun diagnosticoRepository(context: Context) =
        DiagnosticoRepository(DatabaseProvider.getDatabase(context).diagnosticoDao())

    fun actividadRepository(context: Context) =
        ActividadRepository(DatabaseProvider.getDatabase(context).actividadDao())

    fun sensorRepository(context: Context) =
        SensorRepository(DatabaseProvider.getDatabase(context).sensorDao())

    fun usuarioRepository(context: Context) =
        UsuarioRepository(DatabaseProvider.getDatabase(context).usuarioDao())
}
