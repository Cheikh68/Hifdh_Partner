package com.example.hifdhpartner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hifdhpartner.Helpers.DataStoreManager
import com.example.hifdhpartner.databases.FinishVerseDatabase
import com.example.hifdhpartner.databases.FinishVerseQuestion
import com.example.hifdhpartner.databases.UserData
import com.example.hifdhpartner.databases.UserDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = UserDatabase.getInstance(application).userDao()
    private val finishverseDao = FinishVerseDatabase.getInstance(application).finishVerseDao()

    private val _finishVerseQuestion = MutableStateFlow<FinishVerseQuestion?>(null)
    val finishVerseQuestion: StateFlow<FinishVerseQuestion?> = _finishVerseQuestion.asStateFlow()
    private val type1Options = listOf("Option1A", "Option1B", "Option1C", "Option1D")
    private val type2Options = listOf("Option2A", "Option2B", "Option2C", "Option2D")
    private val type3Options = listOf("Option3A", "Option3B", "Option3C", "Option3D")


    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        viewModelScope.launch {
            DataStoreManager.getDarkModeEnabled(application).collect { enabled ->
                _isDarkMode.value = enabled
            }
        }
        initializeDefaultUserData()
        fetchUserData()
    }

    fun toggleDarkMode(context: Context) {
        viewModelScope.launch {
            val newValue = !_isDarkMode.value
            _isDarkMode.value = newValue
            DataStoreManager.setDarkModeEnabled(context, newValue)
        }
    }

    private fun initializeDefaultUserData() {
        viewModelScope.launch {
            val existingData = userDao.getUserData()
            if (existingData == null) {
                val defaultUserData = UserData(
                    dailyMemorizationGoal = "",
                    dailyRevisionGoal = "",
                    weeklyMemorizationGoal = "",
                    weeklyRevisionGoal = "",
                    monthlyMemorizationGoal = "",
                    monthlyRevisionGoal = "",
                    knownSurahs = emptyList(),
                    questionsTaken = 0,
                    questionsRight = 0,
                    questionsWrong = 0
                )
                userDao.insertUserData(defaultUserData)
                fetchUserData()
            }
        }
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _userData.value = userDao.getUserData()
        }
    }

    fun addKnownSurah(surahId: Int) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = currentData.copy(
                knownSurahs = currentData.knownSurahs + surahId
            )
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun removeKnownSurah(surahId: Int) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = currentData.copy(
                knownSurahs = currentData.knownSurahs - surahId
            )
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun updateDailyMemorizationGoal(value: String) {
        updateGoal { it.copy(dailyMemorizationGoal = value) }
    }

    fun updateDailyRevisionGoal(value: String) {
        updateGoal { it.copy(dailyRevisionGoal = value) }
    }

    fun updateWeeklyMemorizationGoal(value: String) {
        updateGoal { it.copy(weeklyMemorizationGoal = value) }
    }

    fun updateWeeklyRevisionGoal(value: String) {
        updateGoal { it.copy(weeklyRevisionGoal = value) }
    }

    fun updateMonthlyMemorizationGoal(value: String) {
        updateGoal { it.copy(monthlyMemorizationGoal = value) }
    }

    fun updateMonthlyRevisionGoal(value: String) {
        updateGoal { it.copy(monthlyRevisionGoal = value) }
    }

    private fun updateGoal(update: (UserData) -> UserData) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = update(currentData)
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun updateQuestionStats(correct: Boolean) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = currentData.copy(
                questionsTaken = currentData.questionsTaken + 1,
                questionsRight = if (correct) currentData.questionsRight + 1 else currentData.questionsRight,
                questionsWrong = if (!correct) currentData.questionsWrong + 1 else currentData.questionsWrong
            )
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    suspend fun populateFinishVerseQuestion(finishVerseQuestions: List<FinishVerseQuestion>){
        finishverseDao.insertAll(finishVerseQuestions)
    }

    suspend fun getRandomFinishVerseQuestion(knownSurahs: List<Int>): FinishVerseQuestion? {
        // Choose a random surah ID from the known list
        val randomSurahId = knownSurahs.randomOrNull() ?: return null
        // Get a random question for that surah
        val questions = finishverseDao.getQuestionsForSurahs(listOf(randomSurahId))
        return questions.randomOrNull()
    }


    fun generateQuestionOptions(question: FinishVerseQuestion): List<String> {
        val options = mutableListOf<String>()

        // Add the correct answer
        options.add(question.correctAnswer)

        // Add one random option from the "specificOptions" list
        options.add(question.specificOptions.random())

        // Add two random options from the appropriate list based on question type
        when (question.type) {
            1 -> options.addAll(type1Options.shuffled().take(2)) // Shuffle and pick 2 options
            2 -> options.addAll(type2Options.shuffled().take(2))
            3 -> options.addAll(type3Options.shuffled().take(2))
        }

        // Shuffle the final list to randomize the position of correct and incorrect answers
        return options.shuffled()
    }

}