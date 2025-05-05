package com.example.hifdhpartner.databases

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverters
import com.example.hifdhpartner.Helpers.StringListConverter


@Entity(tableName = "finish_the_verse_questions")
data class FinishVerseQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,                 // The question prompt (first part of the verse)
    val correctAnswer: String,          // The correct completion (the second part of the verse)
    val type: Int,                      // Used to pick the pool for wrong options
    @TypeConverters(StringListConverter::class)
    val specificOptions: List<String>,  // The pool for close but wrong options
    val surahId: Int                    // New field to link the question to a surah
)


@Dao
interface FinishVerseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<FinishVerseQuestion>)

    @Query("SELECT * FROM finish_the_verse_questions")
    suspend fun getAllQuestions(): List<FinishVerseQuestion>

    @Query("SELECT * FROM finish_the_verse_questions WHERE surahId IN (:surahIds)")
    suspend fun getQuestionsForSurahs(surahIds: List<Int>): List<FinishVerseQuestion>

    @Query("DELETE FROM finish_the_verse_questions")
    suspend fun deleteAllQuestions()
}
