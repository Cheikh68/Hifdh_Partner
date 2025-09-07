package com.example.hifdhpartner.Helpers

import androidx.room.TypeConverter
import java.util.SortedSet

class SortedIntSetConverter {
    @TypeConverter
    fun fromSortedSet(set: SortedSet<Int>): String {
        return set.joinToString(",")
    }

    @TypeConverter
    fun toSortedSet(data: String): SortedSet<Int> {
        return if (data.isEmpty()) sortedSetOf()
        else data.split(",").map { it.toInt() }.toSortedSet()
    }
}
