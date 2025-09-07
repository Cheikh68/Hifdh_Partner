package com.example.hifdhpartner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hifdhpartner.Helpers.DataStoreManager
import com.example.hifdhpartner.databases.AnswerPool
import com.example.hifdhpartner.databases.FinishVerseDao
import com.example.hifdhpartner.databases.FinishVerseQuestion
import com.example.hifdhpartner.databases.MutashaabihaatDao
import com.example.hifdhpartner.databases.MutashaabihaatQuestion
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.databases.RepeatEntry
import com.example.hifdhpartner.databases.Surah
import com.example.hifdhpartner.databases.UserData
import com.example.hifdhpartner.databases.UserDatabase
import com.example.hifdhpartner.databases.Verse
import com.example.hifdhpartner.databases.VerseRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min
import android.util.Log


class ViewModel(
    application: Application,
    private val quranDatabaseHelper: QuranDatabaseHelper,
    private val finishVerseDao: FinishVerseDao,
    private val mutashaabihaatDao: MutashaabihaatDao
) : AndroidViewModel(application) {
    private val userDao = UserDatabase.getInstance(application).userDao()
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData
    private val _previousPlan = MutableStateFlow<List<String>>(emptyList())
    val previousPlan: StateFlow<List<String>> = _previousPlan

    private val _allFinishVerseQuestions = MutableStateFlow<List<FinishVerseQuestion>>(emptyList())
    private val _filteredFinishVerseQuestions = MutableStateFlow<List<FinishVerseQuestion>>(emptyList())
    val filteredFinishVerseQuestions: StateFlow<List<FinishVerseQuestion>> = _filteredFinishVerseQuestions.asStateFlow()
    var answerPools: Map<Int, List<String>> = emptyMap()

    private val _allMutashaabihaatQuestions = MutableStateFlow<List<MutashaabihaatQuestion>>(emptyList())
    private val _filteredMutashaabihaatQuestions = MutableStateFlow<List<MutashaabihaatQuestion>>(emptyList())
    val filteredMutashaabihaatQuestions: StateFlow<List<MutashaabihaatQuestion>> = _filteredMutashaabihaatQuestions.asStateFlow()

    private val knownSurahSet: Set<Int>
        get() = userData.value?.knownSurahs?.toSet() ?: emptySet()
    private val knownVerseSet: Set<VerseRef>
        get() = userData.value?.knownVerses?.toSet() ?: emptySet()
    private val knownPagesSet: Set<Int>
        get() = userData.value?.knownPages?.toSet() ?: emptySet()
    private val surahIdToNameMap: Map<Int, String> by lazy { quranDatabaseHelper.getSurahSummaries().toMap() }
    private val verseIdToPage: Map<Int, Int> by lazy { quranDatabaseHelper.getVerseIdToPageMap() }
    private val identicalVerseMap: Map<VerseRef, Set<VerseRef>> by lazy { loadIdenticalVerses() }



    // Initializing DBs, dark mode and user data
    init {
        startDarkTheme(application)
        loadAnswerPoolsOnce(application)
        initializeDefaultUserData()
        fetchUserData()
    }

    private fun startDarkTheme(application: Application) {
        viewModelScope.launch {
            DataStoreManager.getDarkModeEnabled(application).collect { enabled ->
                _isDarkMode.value = enabled
            }
        }
    }

    private fun loadFinishVerseQuestions() {
        viewModelScope.launch {
            val questions = finishVerseDao.getAllQuestions()
            _allFinishVerseQuestions.value = questions
            _filteredFinishVerseQuestions.value = questions // initialize filtered list
        }
    }

    private fun loadMutashaabihaatQuestions() {
        viewModelScope.launch {
            val questions = mutashaabihaatDao.getAllQuestions()
            _allMutashaabihaatQuestions.value = questions
            _filteredMutashaabihaatQuestions.value = questions // initialize filtered list
        }
    }

    private fun loadAnswerPoolsOnce(application: Application) {
        if (answerPools.isEmpty()) {
            answerPools = loadAnswerPools(application.applicationContext)
        }
    }

    private fun loadIdenticalVerses(): Map<VerseRef, Set<VerseRef>> {
        val context = getApplication<Application>()
        val inputStream = context.assets.open("repeated_verses.json")
        val jsonText = inputStream.bufferedReader().use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<RepeatEntry>>() {}.type
        val entries: List<RepeatEntry> = gson.fromJson(jsonText, type)

        val map = mutableMapOf<VerseRef, MutableSet<VerseRef>>()

        for (entry in entries) {
            for (ref in entry.refs) {
                val otherRefs = entry.refs.filterNot { it == ref }.toSet()
                map.getOrPut(ref) { mutableSetOf() }.addAll(otherRefs)
            }
        }

        return map
    }



    // Dark Mode
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    fun toggleDarkMode(context: Context) {
        viewModelScope.launch {
            val newValue = !_isDarkMode.value
            _isDarkMode.value = newValue
            DataStoreManager.setDarkModeEnabled(context, newValue)
        }
    }



    // User data manipulation
    private fun initializeDefaultUserData() {
        viewModelScope.launch {
            val existingData = userDao.getUserData()
            if (existingData == null) {
                val defaultUserData = UserData(
                    knownVerses = emptyList(),
                    previousPlan = emptyList(),
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
            _previousPlan.value = userData.value?.previousPlan ?: emptyList()
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

    fun setNewPlan(newPlan: List<String>) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = currentData.copy(previousPlan = newPlan)
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun isCorrectAnswer(correct: VerseRef, userAnswerSurahId: Int): Boolean {
        if (correct.surahId == userAnswerSurahId) return true

        val alternatives = identicalVerseMap[correct] ?: return false
        return alternatives.any { it.surahId == userAnswerSurahId }
    }

    fun knowsVerses(): Boolean {
        return knownVerseSet.isNotEmpty()
    }



    // User Known stuff
    fun addKnownSurah(surahId: Int, verses: List<Verse>) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch

            if (surahId in currentData.knownSurahs) return@launch

            val updatedKnownSurahs = currentData.knownSurahs + surahId

            val existingVerseSet = currentData.knownVerses.toSet()
            val newKnownVerses = verses
                .map { VerseRef(surahId, it.id) }
                .filterNot { it in existingVerseSet }

            val updatedKnownVerses = currentData.knownVerses + newKnownVerses

            // Also update pages
            val pageSet = verses.map { it.page_number }.toSet()
            val updatedKnownPages = (currentData.knownPages + pageSet).toSortedSet()

            val updatedData = currentData.copy(
                knownSurahs = updatedKnownSurahs.toSortedSet(),
                knownVerses = updatedKnownVerses,
                knownPages = updatedKnownPages
            )

            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun removeKnownSurah(surahId: Int, verses: List<Verse>) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch

            val verseRefsToRemove = verses.map { VerseRef(surahId, it.id) }.toSet()
            val pagesToCheck = verses.map { it.page_number }.toSet()

            val updatedKnownVerses = currentData.knownVerses.filterNot { it in verseRefsToRemove }
            val updatedKnownVerseSet = updatedKnownVerses.toSet()

            // For each page touched, see if it still contains any known verse
            val pagesToKeep = pagesToCheck.filter { page ->
                updatedKnownVerseSet.any { ref ->
                    verseIdToPage[ref.verseId] == page
                }
            }.toSortedSet()

            val finalKnownPages = (currentData.knownPages - pagesToCheck) + pagesToKeep

            val updatedData = currentData.copy(
                knownSurahs = (currentData.knownSurahs - surahId).toSortedSet(),
                knownVerses = updatedKnownVerses,
                knownPages = finalKnownPages.toSortedSet()
            )

            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun addKnownVerse(surahId: Int, verseId: Int, pageNumber: Int) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch

            val updatedKnownVerses = (currentData.knownVerses.toSet() + VerseRef(surahId, verseId)).toList()
            val updatedKnownSurahs = currentData.knownSurahs.toMutableSet()
            val updatedKnownPages = (currentData.knownPages + pageNumber).toSortedSet()

            // Check if all verses of the surah are now known
            val allSurahVerses = getVersesForSurah(surahId)
            val allKnown = allSurahVerses.all { verse ->
                VerseRef(surahId, verse.id) in updatedKnownVerses.toSet()
            }

            if (allKnown) updatedKnownSurahs.add(surahId)

            val updatedData = currentData.copy(
                knownVerses = updatedKnownVerses,
                knownSurahs = updatedKnownSurahs.toSortedSet(),
                knownPages = updatedKnownPages,
            )

            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }

    fun removeKnownVerse(surahId: Int, verseId: Int, pageNumber: Int) {
        viewModelScope.launch {
            val currentData = userDao.getUserData() ?: return@launch
            val updatedKnownVerses = currentData.knownVerses - VerseRef(surahId, verseId)
            val updatedKnownSurahs = if (surahId in currentData.knownSurahs) {
                currentData.knownSurahs - surahId
            } else {
                currentData.knownSurahs
            }
            val remainingOnPage = knownVerseSet
                .filter { it.surahId == surahId && it != VerseRef(surahId, verseId) }
                .count { ref -> verseIdToPage[ref.verseId] == pageNumber }

            Log.d("test", verseIdToPage.toString())


            val updatedKnownPages = if (remainingOnPage == 0)
                (currentData.knownPages - pageNumber).toSortedSet()
            else
                currentData.knownPages

            val updatedData = currentData.copy(
                knownVerses = updatedKnownVerses,
                knownSurahs = updatedKnownSurahs.toSortedSet(),
                knownPages = updatedKnownPages
            )

            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }



    // Finish the Verse Helper functions
    fun populateFinishVerseIfNeeded(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("QuranAppPrefs", Context.MODE_PRIVATE)
            val isPopulated = prefs.getBoolean("isFinishVerseDatabasePopulated", false)

            if (!isPopulated) {
                val questions = loadFinishVerseQuestionsFromAssets(context)
                finishVerseDao.insertAll(questions)

                // Update state
                _allFinishVerseQuestions.value = questions
                _filteredFinishVerseQuestions.value = questions

                prefs.edit().putBoolean("isFinishVerseDatabasePopulated", true).apply()
            } else {
                loadFinishVerseQuestions()
            }
        }
    }

    private fun loadFinishVerseQuestionsFromAssets(context: Context): List<FinishVerseQuestion> {
        val json = context.assets.open("FinishVerse.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<FinishVerseQuestion>>() {}.type)
    }

    suspend fun getRandomFinishVerseQuestionFromKnownVerses(): FinishVerseQuestion? {
        val knownRefs = userDao.getUserData()?.knownVerses?.toSet() ?: return null
        val allQuestions = finishVerseDao.getAllQuestions()

        val eligible = allQuestions.filter { question ->
            knownRefs.contains(VerseRef(question.surahId, question.verseId))
        }

        return eligible.randomOrNull()
    }

    fun generateQuestionOptions(question: FinishVerseQuestion): List<String> {
        val options = mutableListOf<String>()

        // Add the correct answer
        options.add(question.correctAnswer)

        // Add one random option from the "specificOptions" list
        options.add(question.specificOptions.random())

        // Add two random options from the general answer pool
        answerPools[question.type]?.shuffled()?.take(2)?.let {
            options.addAll(it)
        }

        // Shuffle the final list to randomize answer positions
        return options.shuffled()
    }

    fun loadAnswerPools(context: Context): Map<Int, List<String>> {
        val json = context.assets.open("answer_pools.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val type = object : TypeToken<List<AnswerPool>>() {}.type
        val pools: List<AnswerPool> = gson.fromJson(json, type)
        return pools.associate { it.type to it.options }
    }

    fun clearFinishVerseSort() {
        _filteredFinishVerseQuestions.value = _allFinishVerseQuestions.value
    }

    fun sortFinishVerseBy(criteria: String) {
        val questions = _allFinishVerseQuestions.value

        _filteredFinishVerseQuestions.value = when (criteria.lowercase()) {
            "surah" -> questions.sortedBy { it.surahId } // ascending from 1 to 114
            "type" -> questions.sortedBy { it.type }
            else -> questions
        }
    }

    fun filterToKnownVersesOnly() {
        viewModelScope.launch {
            val user = userDao.getUserData() ?: return@launch
            val knownRefs = user.knownVerses.toSet()
            val questions = _allFinishVerseQuestions.value

            _filteredFinishVerseQuestions.value = questions.filter { q ->
                knownRefs.contains(VerseRef(q.surahId, q.verseId))
            }
        }
    }

    fun filterFinishVerseToSpecificSurah(surahId: Int) {
        viewModelScope.launch{
            _filteredFinishVerseQuestions.value = finishVerseDao.getQuestionsForSurah(surahId)
        }
    }



    // Mutashaabihaat Helper Functions
    fun populateMutashaabihaatIfNeeded(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("QuranAppPrefs", Context.MODE_PRIVATE)
            val isPopulated = prefs.getBoolean("isMutashaabihaatDatabasePopulated", false)

            if (!isPopulated) {
                val questions = loadMutashaabihaatQuestionsFromAssets(context)
                mutashaabihaatDao.insertAll(questions)

                // Update state
                _allMutashaabihaatQuestions.value = questions
                _filteredMutashaabihaatQuestions.value = questions

                prefs.edit().putBoolean("isMutashaabihaatDatabasePopulated", true).apply()
            } else {
                loadMutashaabihaatQuestions()
            }
        }
    }

    private fun loadMutashaabihaatQuestionsFromAssets(context: Context): List<MutashaabihaatQuestion> {
        val json = context.assets.open("Mutashaabihaat.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<MutashaabihaatQuestion>>() {}.type)
    }

    suspend fun getRandomMutashaabihaatQuestionFromKnownVerses(): MutashaabihaatQuestion? {
        val knownRefs = userDao.getUserData()?.knownVerses?.toSet() ?: return null
        val allQuestions = mutashaabihaatDao.getAllQuestions()

        val eligible = allQuestions.filter { question ->
            question.references.any { ref -> knownRefs.contains(ref) }
        }

        return eligible.randomOrNull()
    }

    fun sortMutashaabihaatBy(criteria: String) {
        val questions = _allMutashaabihaatQuestions.value

        _filteredMutashaabihaatQuestions.value = when (criteria.lowercase()) {
            "surah" -> questions.sortedBy {
                it.references.minOfOrNull { ref -> ref.surahId } ?: Int.MAX_VALUE
            }
            else -> questions
        }
    }

    fun filterMutashaabihaatToKnownOnly() {
        viewModelScope.launch {
            val user = userDao.getUserData() ?: return@launch
            val knownSurahs = user.knownSurahs.toSet()
            val questions = _allMutashaabihaatQuestions.value

            _filteredMutashaabihaatQuestions.value = questions.filter { q ->
                q.references.any { it.surahId in knownSurahs }
            }
        }
    }

    fun clearMutashaabihaatSort() {
        _filteredMutashaabihaatQuestions.value = _allMutashaabihaatQuestions.value
    }

    fun filterMutashaabihaatToSpecificSurah(surahId: Int) {
        viewModelScope.launch {
            _filteredMutashaabihaatQuestions.value =
                _allMutashaabihaatQuestions.value.filter { it.surahs.contains(surahId) }
        }
    }

    fun updateMutashaabihaatStats(userSelections: Map<String, VerseRef?>, correctMatches: Map<String, VerseRef>) {
        viewModelScope.launch {
            var correctCount = 0
            var wrongCount = 0

            correctMatches.forEach { (key, correctRef) ->
                val userSelected = userSelections[key]
                if (userSelected != null) {
                    if (userSelected == correctRef) {
                        correctCount++
                    } else {
                        wrongCount++
                    }
                }
            }

            val currentData = userDao.getUserData() ?: return@launch
            val updatedData = currentData.copy(
                questionsTaken = currentData.questionsTaken + correctCount + wrongCount,
                questionsRight = currentData.questionsRight + correctCount,
                questionsWrong = currentData.questionsWrong + wrongCount
            )
            userDao.updateUserData(updatedData)
            fetchUserData()
        }
    }



    // Quran DB Helper functions
    fun getAllSurahs(): List<Surah> {
        return quranDatabaseHelper.getAllSurahs()
    }

    fun getVersesForSurah(surahId: Int): List<Verse> {
        return quranDatabaseHelper.getVersesForSurah(surahId)
    }

    enum class SurahState {
        NotStarted, InProgress, Completed
    }

    fun getSurahState(surah: Surah): SurahState {
        return when {
            surah.id in knownSurahSet -> SurahState.Completed
            knownVerseSet.any { it.surahId == surah.id } -> SurahState.InProgress
            else -> SurahState.NotStarted
        }
    }

    fun getSurahById(id: Int): Surah? {
        return quranDatabaseHelper.getSurahById(id)
    }

    fun getRandomKnownVerse(): Pair<Surah, Verse>? {
        val verseRef = knownVerseSet.randomOrNull() ?: return null
        val surah = getSurahById(verseRef.surahId) ?: return null
        val verse = surah.verses.find { it.id == verseRef.verseId } ?: return null
        return Pair(surah, verse)
    }

    fun dividePagesIntoRevisionPlan(cycleLength: Int): Triple<Pair<List<List<Int>>, List<Int>>, Int, Int> {
        val pages = knownPagesSet.sorted()
        val totalPages = pages.size

        if (totalPages == 0 || cycleLength <= 0)
            return Triple(emptyList<List<Int>>() to emptyList(), 0, 0)

        val usableDays = min(cycleLength, totalPages)
        val mainChunkSize = totalPages / usableDays
        val remainderCount = totalPages % usableDays

        // Leave remainder out for separate display
        val fullChunks = pages.dropLast(remainderCount).chunked(mainChunkSize)
        val remainder = if (remainderCount > 0) pages.takeLast(remainderCount) else emptyList()

        return Triple(fullChunks to remainder, mainChunkSize, remainderCount)
    }

    fun getSurahNameFromId(id: Int): String {
        return surahIdToNameMap[id] ?: "Unknown Surah"
    }
}


class MyViewModelFactory (
    private val application: Application,
    private val quranDatabaseHelper: QuranDatabaseHelper,
    private val finishVerseDao: FinishVerseDao,
    private val mutashaabihaatDao: MutashaabihaatDao
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return ViewModel(application, quranDatabaseHelper, finishVerseDao, mutashaabihaatDao) as T
    }
}