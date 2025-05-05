package com.example.hifdhpartner

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hifdhpartner.OtherScreens.Goals
import com.example.hifdhpartner.OtherScreens.MainScreen
import com.example.hifdhpartner.OtherScreens.ProgressTracker
import com.example.hifdhpartner.OtherScreens.Settings
import com.example.hifdhpartner.OtherScreens.Suggestions
import com.example.hifdhpartner.OtherScreens.UserProfile
import com.example.hifdhpartner.databases.FinishVerseQuestion
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.tests.ComprehensionTest
import com.example.hifdhpartner.tests.FinishVerse
import com.example.hifdhpartner.tests.Mutashaabihaat
import com.example.hifdhpartner.tests.StrengthTest
import com.example.hifdhpartner.ui.theme.HifdhPartnerTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var quranDatabaseHelper: QuranDatabaseHelper
    private val MyViewModel: ViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database helper
        quranDatabaseHelper = QuranDatabaseHelper(this)

        // Check and populate the database
        populateDatabaseIfNeeded()

        setContent {
            val isDarkMode by MyViewModel.isDarkMode.collectAsState(initial = false)

            HifdhPartnerTheme(darkTheme = isDarkMode) {
                Navigation_graph(mainActivity = this)
            }
        }
    }

    private fun populateDatabaseIfNeeded() {
        val sharedPreferences = getSharedPreferences("QuranAppPrefs", Context.MODE_PRIVATE)
        val isDatabasePopulated = sharedPreferences.getBoolean("isDatabasePopulated", false)

        if (!isDatabasePopulated) {
            quranDatabaseHelper.insertQuranData(this)

            val finishVerseQuestions = loadFinishVerseQuestionsFromAssets(this)
            lifecycleScope.launch {
                MyViewModel.populateFinishVerseQuestion(finishVerseQuestions)
            }

            sharedPreferences.edit().putBoolean("isDatabasePopulated", true).apply()
        }
    }

    fun loadFinishVerseQuestionsFromAssets(context: Context): List<FinishVerseQuestion> {
        val jsonString = context.assets.open("FinishVerse.json")
            .bufferedReader()
            .use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<FinishVerseQuestion>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun getDatabaseHelper(): QuranDatabaseHelper = quranDatabaseHelper
    fun getViewModel(): ViewModel = MyViewModel
}


@Composable
fun Navigation_graph(mainActivity: MainActivity) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") { MainScreen(navController = navController) }
            composable("progress") { ProgressTracker(navController = navController, databaseHelper = mainActivity.getDatabaseHelper(), viewModel = mainActivity.getViewModel()) }
            composable("goals") { Goals(navController = navController, viewModel = mainActivity.getViewModel()) }
            composable("suggestions") { Suggestions(navController = navController) }
            composable("userprofile") { UserProfile(navController = navController, viewModel = mainActivity.getViewModel()) }
            composable("strentest") { StrengthTest(navController = navController, databaseHelper = mainActivity.getDatabaseHelper(), viewModel = mainActivity.getViewModel()) }
            composable("comptest") { ComprehensionTest(navController = navController, databaseHelper = mainActivity.getDatabaseHelper(), viewModel = mainActivity.getViewModel()) }
            composable("mutashaabtest") { Mutashaabihaat(navController = navController, databaseHelper = mainActivity.getDatabaseHelper(), viewModel = mainActivity.getViewModel()) }
            composable("finishtest") { FinishVerse(navController = navController, databaseHelper = mainActivity.getDatabaseHelper(), viewModel = mainActivity.getViewModel()) }
            composable("settings") { Settings(navController = navController, viewModel = mainActivity.getViewModel()) }
        }
    }
}