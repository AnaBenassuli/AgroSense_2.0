package com.agrosense.app.data.local.converter

import androidx.room.TypeConverter
import com.agrosense.app.data.local.entity.Modulo
import com.agrosense.app.data.local.entity.Prediccion

class Converters {

    @TypeConverter
    fun top3ToString(value: List<Prediccion>?): String {
        return value.orEmpty().joinToString(";;") {
            val claseSegura = it.clase
                .replace(";;", " ")
                .replace("::", " ")

            "$claseSegura::${it.confianza}"
        }
    }

    @TypeConverter
    fun stringToTop3(value: String?): List<Prediccion> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }

        return value.split(";;").mapNotNull { item ->
            val parts = item.split("::")

            if (parts.size != 2) {
                return@mapNotNull null
            }

            val confianza = parts[1].toFloatOrNull()
                ?: return@mapNotNull null

            Prediccion(
                clase = parts[0],
                confianza = confianza
            )
        }
    }

    @TypeConverter
    fun moduloToString(value: Modulo): String = value.name

    // Lo desconocido se lee como CULTIVO, que es lo que eran todas las filas anteriores a la v3
    @TypeConverter
    fun stringToModulo(value: String?): Modulo =
        Modulo.entries.firstOrNull { it.name == value } ?: Modulo.CULTIVO
}