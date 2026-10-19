package com.agrosense.app.data.local

import android.content.Context
import androidx.room.Room
import com.agrosense.app.data.local.migration.TODAS_LAS_MIGRACIONES

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AgroSenseDatabase? = null

    fun getDatabase(context: Context): AgroSenseDatabase {
        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AgroSenseDatabase::class.java,
                "agrosense.db"
            )
                .addMigrations(*TODAS_LAS_MIGRACIONES)
                .build()

            INSTANCE = instance
            instance
        }
    }
}