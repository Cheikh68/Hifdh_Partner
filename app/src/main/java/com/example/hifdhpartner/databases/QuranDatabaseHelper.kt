package com.example.hifdhpartner.databases


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.hifdhpartner.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class QuranDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "quran_en"  // Database file name
        private const val DATABASE_VERSION = 1  // Database version
    }

    // Create tables when the database is first created
    override fun onCreate(db: SQLiteDatabase) {
        val createSurahTable = """
            CREATE TABLE Surah (
                id INTEGER PRIMARY KEY,
                name TEXT,
                transliteration TEXT,
                translation TEXT,
                type TEXT,
                total_verses INTEGER
            )
        """
        val createVerseTable = """
            CREATE TABLE Verse (
                id INTEGER,
                surah_id INTEGER,
                text TEXT,
                translation TEXT,
                PRIMARY KEY (id, surah_id),
                FOREIGN KEY (surah_id) REFERENCES Surah(id)
            )
        """

        db.execSQL(createSurahTable)
        db.execSQL(createVerseTable)
    }

    // Handle database version upgrades
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Surah")
        db.execSQL("DROP TABLE IF EXISTS Verse")
        onCreate(db)
    }

    // Example method to insert Surah data
    fun insertSurah(surah: Surah) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("id", surah.id)
            put("name", surah.name)
            put("transliteration", surah.transliteration)
            put("translation", surah.translation)
            put("type", surah.type)
            put("total_verses", surah.total_verses)
        }
        db.insert("Surah", null, contentValues)
    }

    fun parseQuranJson(context: Context): List<Surah> {
        val jsonString = context.resources.openRawResource(R.raw.quran_en).bufferedReader().use { it.readText() }
        val quranListType = object : TypeToken<List<Surah>>() {}.type
        val surahs = Gson().fromJson<List<Surah>>(jsonString, quranListType)
        return surahs
    }



    fun insertQuranData(context: Context) {
        val sharedPreferences = context.getSharedPreferences("QuranAppPrefs", Context.MODE_PRIVATE)
        val isDataInserted = sharedPreferences.getBoolean("isDataInserted", false)

        val surahs = parseQuranJson(context)
        val db = writableDatabase
        for (surah in surahs) {
            insertSurah(surah)
            for (verse in surah.verses) {
                Log.d("DatabaseInsert", "Inserting Verse: ID=${verse.id}, Surah ID=${surah.id}")
                val verseContentValues = ContentValues().apply {
                    put("id", verse.id)
                    put("surah_id", surah.id)
                    put("text", verse.text)
                    put("translation", verse.translation)
                }
                db.insert("Verse", null, verseContentValues)
            }
        }

        // Set the flag in SharedPreferences to true
        sharedPreferences.edit().putBoolean("isDataInserted", true).apply()
        db.close()


        /*
        if (!isDataInserted) {
            val surahs = parseQuranJson(context)
            val db = writableDatabase
            for (surah in surahs) {
                insertSurah(surah)
                for (verse in surah.verses) {
                    Log.d("DatabaseInsert", "Inserting Verse: ID=${verse.id}, Surah ID=${surah.id}")
                    val verseContentValues = ContentValues().apply {
                        put("id", verse.id)
                        put("surah_id", surah.id)
                        put("text", verse.text)
                        put("translation", verse.translation)
                    }
                    db.insert("Verse", null, verseContentValues)
                }
            }

            // Set the flag in SharedPreferences to true
            sharedPreferences.edit().putBoolean("isDataInserted", true).apply()
            db.close()
        }

         */
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

        if (idIndex >= 0 && nameIndex >= 0 && transliterationIndex >= 0 &&
            translationIndex >= 0 && typeIndex >= 0 && totalVersesIndex >= 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                val transliteration = cursor.getString(transliterationIndex)
                val translation = cursor.getString(translationIndex)
                val type = cursor.getString(typeIndex)
                val totalVerses = cursor.getInt(totalVersesIndex)

                surahs.add(
                    Surah(
                        id = id,
                        name = name,
                        transliteration = transliteration,
                        translation = translation,
                        type = type,
                        total_verses = totalVerses,
                        verses = emptyList() // Skip loading verses for simplicity
                    )
                )
            }
        }
        cursor.close()
        return surahs
    }

    fun getVersesFromSurah(surahId: Int): List<String> {
        val verses = mutableListOf<String>()
        val db = readableDatabase
        // Query verses by surah_id
        val cursor = db.rawQuery(
            "SELECT text FROM Verse WHERE surah_id = ? ORDER BY id ASC",
            arrayOf(surahId.toString())
        )
        while (cursor.moveToNext()) {
            verses.add(cursor.getString(0))
        }
        cursor.close()

        Log.d("DatabaseDebug", "Fetching verses for Surah ID: $surahId")
        if (verses.isEmpty()) {
            Log.d("DatabaseDebug", "No verses found for Surah ID: $surahId")
        }

        return verses
    }



    fun getSurahById(id: Int): Surah? {
        val db = readableDatabase

        // Query the Surah table to get the Surah by its id
        val cursor = db.query(
            "Surah", null, "id = ?", arrayOf(id.toString()), null, null, null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex("id")
            val nameIndex = cursor.getColumnIndex("name")
            val transliterationIndex = cursor.getColumnIndex("transliteration")
            val translationIndex = cursor.getColumnIndex("translation")
            val typeIndex = cursor.getColumnIndex("type")
            val totalVersesIndex = cursor.getColumnIndex("total_verses")

            if (idIndex != -1 && nameIndex != -1 && transliterationIndex != -1 &&
                translationIndex != -1 && typeIndex != -1 && totalVersesIndex != -1) {

                val surah = Surah(
                    cursor.getInt(idIndex),
                    cursor.getString(nameIndex),
                    cursor.getString(transliterationIndex),
                    cursor.getString(translationIndex),
                    cursor.getString(typeIndex),
                    cursor.getInt(totalVersesIndex),
                    getVersesForSurah(cursor.getInt(idIndex))  // Retrieve verses for the Surah
                )
                surah
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getVersesForSurah(surahId: Int): List<Verse> {
        val db = readableDatabase

        // Query the Verse table for verses associated with the given surah_id
        val cursor = db.query(
            "Verse", null, "surah_id = ?", arrayOf(surahId.toString()), null, null, null
        )

        val verses = mutableListOf<Verse>()
        while (cursor != null && cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val textIndex = cursor.getColumnIndex("text")
            val translationIndex = cursor.getColumnIndex("translation")

            if (idIndex != -1 && textIndex != -1 && translationIndex != -1) {
                verses.add(
                    Verse(
                        cursor.getInt(idIndex),
                        cursor.getString(textIndex),
                        cursor.getString(translationIndex)
                    )
                )
            }
        }
        cursor?.close()
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

        return if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex("id")
            val textIndex = cursor.getColumnIndex("text")
            val translationIndex = cursor.getColumnIndex("translation")

            if (idIndex != -1 && textIndex != -1 && translationIndex != -1) {
                Verse(
                    cursor.getInt(idIndex),
                    cursor.getString(textIndex),
                    cursor.getString(translationIndex)
                )
            } else {
                null
            }
        } else {
            null
        }.also { cursor?.close() }
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