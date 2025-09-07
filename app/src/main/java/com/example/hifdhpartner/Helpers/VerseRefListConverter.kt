package com.example.hifdhpartner.Helpers

import androidx.room.TypeConverter
import com.example.hifdhpartner.databases.VerseRef

class VerseRefListConverter {

    @TypeConverter
    fun fromVerseRefList(list: List<VerseRef>): String {
        return list.joinToString(";") { "${it.surahId},${it.verseId}" }
    }

    @TypeConverter
    fun toVerseRefList(data: String): List<VerseRef> {
        if (data.isBlank()) return emptyList()
        return data.split(";").map {
            val (surahId, verseId) = it.split(",")
            VerseRef(surahId.toInt(), verseId.toInt())
        }
    }
}