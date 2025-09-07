package com.example.hifdhpartner.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hifdhpartner.Helpers.MutashaabihaatConverters
import com.example.hifdhpartner.Helpers.StringListConverter

@Database(entities = [MutashaabihaatQuestion::class], version = 1)
@TypeConverters(MutashaabihaatConverters::class)
abstract class MutashaabihaatDatabase : RoomDatabase() {
    abstract fun MutashaabihaatDao(): MutashaabihaatDao

    companion object {
        @Volatile
        private var INSTANCE: MutashaabihaatDatabase? = null

        fun getInstance(context: Context): MutashaabihaatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MutashaabihaatDatabase::class.java,
                    "mutashaabihaat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}