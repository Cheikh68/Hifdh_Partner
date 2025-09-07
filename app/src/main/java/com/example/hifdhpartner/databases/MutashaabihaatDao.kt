package com.example.hifdhpartner.databases

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "mutashaabihaat_questions")
data class MutashaabihaatQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val passages: List<String>,
    val references: List<VerseRef>,
    val surahs: List<Int>,
    val correctMatches: Map<String, VerseRef>,
    val type: String? = null,
    val explanation: String? = null
)

@Dao
interface MutashaabihaatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<MutashaabihaatQuestion>)

    @Query("SELECT * FROM mutashaabihaat_questions")
    suspend fun getAllQuestions(): List<MutashaabihaatQuestion>

    @Query("DELETE FROM mutashaabihaat_questions")
    suspend fun clearAll()
}
