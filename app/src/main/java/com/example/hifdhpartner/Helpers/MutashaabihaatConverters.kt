package com.example.hifdhpartner.Helpers

import androidx.room.TypeConverter
import com.example.hifdhpartner.databases.VerseRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MutashaabihaatConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromVerseRefList(value: List<VerseRef>): String = gson.toJson(value)

    @TypeConverter
    fun toVerseRefList(value: String): List<VerseRef> =
        gson.fromJson(value, object : TypeToken<List<VerseRef>>() {}.type)

    @TypeConverter
    fun fromIntList(value: List<Int>): String = gson.toJson(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromMap(value: Map<String, VerseRef>): String = gson.toJson(value)

    @TypeConverter
    fun toMap(value: String): Map<String, VerseRef> =
        gson.fromJson(value, object : TypeToken<Map<String, VerseRef>>() {}.type)
}
