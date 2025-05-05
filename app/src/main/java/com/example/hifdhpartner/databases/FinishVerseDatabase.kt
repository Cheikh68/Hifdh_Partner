package com.example.hifdhpartner.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hifdhpartner.Helpers.StringListConverter


@Database(entities = [FinishVerseQuestion::class], version = 1, exportSchema = false)
@TypeConverters(StringListConverter::class) // Register the StringListConverter here
abstract class FinishVerseDatabase : RoomDatabase() {
    abstract fun finishVerseDao(): FinishVerseDao

    companion object {
        @Volatile
        private var INSTANCE: FinishVerseDatabase? = null

        fun getInstance(context: Context): FinishVerseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinishVerseDatabase::class.java,
                    "finish_verse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
