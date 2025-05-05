package com.example.hifdhpartner.OtherScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    // Collect the user data from the ViewModel
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

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Display user data from the database
                userData?.let {
                    Text(text = "Daily Memorization Goal: ${it.dailyMemorizationGoal}")
                    Text(text = "Daily Revision Goal: ${it.dailyRevisionGoal}")
                    Text(text = "Weekly Memorization Goal: ${it.weeklyMemorizationGoal}")
                    Text(text = "Weekly Revision Goal: ${it.weeklyRevisionGoal}")
                    Text(text = "Monthly Memorization Goal: ${it.monthlyMemorizationGoal}")
                    Text(text = "Monthly Revision Goal: ${it.monthlyRevisionGoal}")
                    Text(text = "Questions Taken: ${it.questionsTaken}")
                    Text(text = "Questions Correct: ${it.questionsRight}")
                    Text(text = "Questions Wrong: ${it.questionsWrong}")

                    // Display known Surahs
                    Text(
                        text = "Known Surahs: ${
                            if (it.knownSurahs.isEmpty()) "None" else it.knownSurahs.joinToString(", ")
                        }"
                    )
                } ?: run {
                    // Display a loading or empty state if data is unavailable
                    Text(text = "Loading user data...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
