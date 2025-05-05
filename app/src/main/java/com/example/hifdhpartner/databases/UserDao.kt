package com.example.hifdhpartner.databases

import androidx.room.*
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.hifdhpartner.Helpers.IntListConverter


@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dailyMemorizationGoal: String,
    val dailyRevisionGoal: String,
    val weeklyMemorizationGoal: String,
    val weeklyRevisionGoal: String,
    val monthlyMemorizationGoal: String,
    val monthlyRevisionGoal: String,
    @TypeConverters(IntListConverter::class)
    val knownSurahs: List<Int>,
    val questionsTaken: Int,
    val questionsRight: Int,
    val questionsWrong: Int
)



@Dao
interface UserDao {
    @Query("SELECT * FROM user_data LIMIT 1")
    suspend fun getUserData(): UserData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserData)

    @Update
    suspend fun updateUserData(userData: UserData)

    @Query("DELETE FROM user_data")
    suspend fun deleteAllUserData()
}
