package com.horaswachtman.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.horaswachtman.app.data.dao.DayWorkDao
import com.horaswachtman.app.data.entity.DayWork

@Database(entities = [DayWork::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dayWorkDao(): DayWorkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horaswachtman_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
