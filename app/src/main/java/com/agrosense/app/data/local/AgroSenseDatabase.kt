package com.agrosense.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agrosense.app.data.local.converter.Converters
import com.agrosense.app.data.local.dao.ActividadDao
import com.agrosense.app.data.local.dao.DiagnosticoDao
import com.agrosense.app.data.local.dao.ParcelaDao
import com.agrosense.app.data.local.dao.SensorDao
import com.agrosense.app.data.local.dao.UsuarioDao
import com.agrosense.app.data.local.entity.Actividad
import com.agrosense.app.data.local.entity.Diagnostico
import com.agrosense.app.data.local.entity.Parcela
import com.agrosense.app.data.local.entity.Sensor
import com.agrosense.app.data.local.entity.Usuario

@Database(
    entities = [
        Usuario::class,
        Parcela::class,
        Diagnostico::class,
        Actividad::class,
        Sensor::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AgroSenseDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun parcelaDao(): ParcelaDao
    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun sensorDao(): SensorDao
}