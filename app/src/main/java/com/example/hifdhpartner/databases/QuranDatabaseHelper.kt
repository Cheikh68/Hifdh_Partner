package com.example.hifdhpartner.databases


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream


class QuranDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "quran_en"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // No-op: we're using a pre-populated database from assets
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No-op: handle upgrades manually if needed
    }

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open("$DATABASE_NAME.db").use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun getAllSurahs(): List<Surah> {
        val db = readableDatabase
        val cursor = db.query("Surah", null, null, null, null, null, null)
        val surahs = mutableListOf<Surah>()

        val idIndex = cursor.getColumnIndex("id")
        val nameIndex = cursor.getColumnIndex("name")
        val transliterationIndex = cursor.getColumnIndex("transliteration")
        val translationIndex = cursor.getColumnIndex("translation")
        val typeIndex = cursor.getColumnIndex("type")
        val totalVersesIndex = cursor.getColumnIndex("total_verses")

        while (cursor.moveToNext()) {
            val surah = Surah(
                id = cursor.getInt(idIndex),
                name = cursor.getString(nameIndex),
                transliteration = cursor.getString(transliterationIndex),
                translation = cursor.getString(translationIndex),
                type = cursor.getString(typeIndex),
                total_verses = cursor.getInt(totalVersesIndex),
                verses = emptyList() // Load separately if needed
            )
            surahs.add(surah)
        }
        cursor.close()
        return surahs
    }

    fun getVersesFromSurah(surahId: Int): List<String> {
        val verses = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT text FROM Verse WHERE surah_id = ? ORDER BY id ASC",
            arrayOf(surahId.toString())
        )
        while (cursor.moveToNext()) {
            verses.add(cursor.getString(0))
        }
        cursor.close()
        return verses
    }

    fun getSurahById(id: Int): Surah? {
        val db = readableDatabase
        val cursor = db.query("Surah", null, "id = ?", arrayOf(id.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            val surah = Surah(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                transliteration = cursor.getString(cursor.getColumnIndexOrThrow("transliteration")),
                translation = cursor.getString(cursor.getColumnIndexOrThrow("translation")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                total_verses = cursor.getInt(cursor.getColumnIndexOrThrow("total_verses")),
                verses = getVersesForSurah(id)
            )
            cursor.close()
            surah
        } else {
            cursor.close()
            null
        }
    }

    fun getVersesForSurah(surahId: Int): List<Verse> {
        val db = readableDatabase
        val cursor = db.query("Verse", null, "surah_id = ?", arrayOf(surahId.toString()), null, null, "id ASC")
        val verses = mutableListOf<Verse>()
        while (cursor.moveToNext()) {
            verses.add(
                Verse(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                    translation = cursor.getString(cursor.getColumnIndexOrThrow("translation"))
                )
            )
        }
        cursor.close()
        return verses
    }

    fun getVerseById(surahId: Int, verseId: Int): Verse? {
        val db = readableDatabase
        val cursor = db.query(
            "Verse",
            null,
            "surah_id = ? AND id = ?",
            arrayOf(surahId.toString(), verseId.toString()),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val verse = Verse(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                text = cursor.getString(cursor.getColumnIndexOrThrow("text")),
                translation = cursor.getString(cursor.getColumnIndexOrThrow("translation"))
            )
            cursor.close()
            verse
        } else {
            cursor.close()
            null
        }
    }
}


data class Surah(
    val id: Int,
    val name: String,
    val transliteration: String,
    val translation: String,
    val type: String,
    val total_verses: Int,
    val verses: List<Verse>  // List of verses in the Surah
)

data class Verse(
    val id: Int,
    val text: String,
    val translation: String
)