package com.example.mydrawingapp


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Drawing::class], version = 2, exportSchema = false) // Increment version here
abstract class DrawingDatabase : RoomDatabase() {

    abstract fun drawingDao(): DrawingDao

    companion object {
        @Volatile
        private var INSTANCE: DrawingDatabase? = null

        fun getDatabase(context: Context): DrawingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrawingDatabase::class.java,
                    "drawing_database"
                )
                    .fallbackToDestructiveMigration() // This will rebuild the database on schema changes, clearing data
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

