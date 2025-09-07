package com.example.hifdhpartner.databases

import androidx.room.*
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.hifdhpartner.Helpers.IntListConverter
import com.example.hifdhpartner.Helpers.SortedIntSetConverter
import com.example.hifdhpartner.Helpers.StringListConverter
import com.example.hifdhpartner.Helpers.VerseRefListConverter
import java.util.SortedSet
import kotlin.collections.sortedSetOf


@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @TypeConverters(IntListConverter::class)
    val knownSurahs: SortedSet<Int> = sortedSetOf(),
    @TypeConverters(VerseRefListConverter::class)
    val knownVerses: List<VerseRef>,
    @TypeConverters(SortedIntSetConverter::class)
    val knownPages: SortedSet<Int> = sortedSetOf(),
    @TypeConverters(StringListConverter::class)
    val previousPlan: List<String> = emptyList(),
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
