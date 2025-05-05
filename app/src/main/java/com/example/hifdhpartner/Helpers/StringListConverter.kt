package com.example.hifdhpartner.Helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class StringListConverter {

    // Convert the List<String> to a single String (for storage)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    // Convert the single String back to a List<String> (for retrieval)
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val gson = Gson()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}
