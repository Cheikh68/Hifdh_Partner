package com.example.hifdhpartner.Helpers

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return list.joinToString(",") // Convert List<Int> to a comma-separated string
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        return if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }
    }
}
