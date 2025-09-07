package com.example.hifdhpartner.OtherScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.hifdhpartner.ViewModel


@Composable
fun UserProfile(navController: NavController, viewModel: ViewModel) {
    val userData by viewModel.userData.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            MainText("Profile")
            Spacer(modifier = Modifier.height(16.dp))

            // Display user data from the database
            userData?.let {
                Text(text = "Questions Taken: ${it.questionsTaken}\nQuestions Correct: ${it.questionsRight}\nQuestions Wrong: ${it.questionsWrong}\n\nLatest revision plan: \n${it.previousPlan}\n", color = MaterialTheme.colorScheme.onPrimary)

                // Display known Surahs and Pages
                val knownSurahRanges = compressToRanges(it.knownSurahs.toList())
                val knownPageRanges = compressToRanges(it.knownPages.toList())
                Text(text = "Known Surahs: $knownSurahRanges\nKnown Pages: $knownPageRanges", color = MaterialTheme.colorScheme.onPrimary)
            } ?: run {
                // Display a loading or empty state if data is unavailable
                Text(text = "Loading user data...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
