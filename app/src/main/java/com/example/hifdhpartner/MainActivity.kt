package com.example.hifdhpartner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.hifdhpartner.OtherScreens.Goals
import com.example.hifdhpartner.OtherScreens.MainScreen
import com.example.hifdhpartner.OtherScreens.ProgressTracker
import com.example.hifdhpartner.Reading.Reading
import com.example.hifdhpartner.OtherScreens.Settings
import com.example.hifdhpartner.OtherScreens.UserProfile
import com.example.hifdhpartner.Reading.FinishTheVerseReading
import com.example.hifdhpartner.Reading.MutashaabihaatReading
import com.example.hifdhpartner.databases.FinishVerseDao
import com.example.hifdhpartner.databases.FinishVerseDatabase
import com.example.hifdhpartner.databases.MutashaabihaatDao
import com.example.hifdhpartner.databases.MutashaabihaatDatabase
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.databases.UserDatabase
import com.example.hifdhpartner.tests.ComprehensionTest
import com.example.hifdhpartner.tests.FinishVerse
import com.example.hifdhpartner.tests.Mutashaabihaat
import com.example.hifdhpartner.tests.StrengthTest
import com.example.hifdhpartner.ui.theme.HifdhPartnerTheme


class MainActivity : ComponentActivity() {
    private lateinit var quranDatabaseHelper: QuranDatabaseHelper
    private lateinit var finishVerseDao: FinishVerseDao
    private lateinit var mutashaabihaatDao: MutashaabihaatDao
    private lateinit var myViewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Room database with fallback to destructive migration
        Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "user_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Initialize database helper and Finish The Verse Database instance
        quranDatabaseHelper = QuranDatabaseHelper(applicationContext)
        val db1 = FinishVerseDatabase.getInstance(applicationContext)
        val db2 = MutashaabihaatDatabase.getInstance(applicationContext)
        finishVerseDao = db1.finishVerseDao()
        mutashaabihaatDao = db2.MutashaabihaatDao()

        // Initialize ViewModel with custom factory
        val factory = MyViewModelFactory(application, quranDatabaseHelper, finishVerseDao, mutashaabihaatDao)
        myViewModel = ViewModelProvider(this, factory).get(ViewModel::class.java)

        myViewModel.populateFinishVerseIfNeeded(this)
        myViewModel.populateMutashaabihaatIfNeeded(this)

        // Compose UI
        setContent {
            val isDarkMode by myViewModel.isDarkMode.collectAsState(initial = false)

            HifdhPartnerTheme(darkTheme = isDarkMode) {
                Navigation_graph(MyViewModel = myViewModel)
            }
        }
    }
}

@Composable
fun Navigation_graph(MyViewModel: ViewModel) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") { MainScreen(navController = navController, viewModel = MyViewModel) }
            composable("progress") { ProgressTracker(navController = navController, viewModel = MyViewModel) }
            composable("goals") { Goals(navController = navController, viewModel = MyViewModel) }
            composable("reading") { Reading(navController = navController) }
            composable("finishreading") { FinishTheVerseReading(navController = navController, viewModel = MyViewModel) }
            composable("mutashaabreading") { MutashaabihaatReading(navController = navController, viewModel= MyViewModel) }
            composable("userprofile") { UserProfile(navController = navController, viewModel = MyViewModel) }
            composable("strentest") { StrengthTest(navController = navController, viewModel = MyViewModel) }
            composable("comptest") { ComprehensionTest(navController = navController, viewModel = MyViewModel) }
            composable("mutashaabtest") { Mutashaabihaat(navController = navController, viewModel = MyViewModel) }
            composable("finishtest") { FinishVerse(navController = navController, viewModel = MyViewModel) }
            composable("settings") { Settings(navController = navController, viewModel = MyViewModel) }
        }
    }
}