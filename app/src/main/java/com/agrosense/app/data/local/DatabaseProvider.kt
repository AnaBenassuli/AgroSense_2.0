package com.agrosense.app.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AgroSenseDatabase? = null

    fun getDatabase(context: Context): AgroSenseDatabase {
        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AgroSenseDatabase::class.java,
                "agrosense.db"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}