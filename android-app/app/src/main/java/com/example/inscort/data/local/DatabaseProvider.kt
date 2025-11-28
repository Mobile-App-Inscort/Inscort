// data/local/DatabaseProvider.kt
package com.example.inscort.data.local

import android.content.Context
import androidx.room.Room
import com.example.inscort.data.local.db.AppDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "inscort.db"
            ).build().also { INSTANCE = it }
        }
}
