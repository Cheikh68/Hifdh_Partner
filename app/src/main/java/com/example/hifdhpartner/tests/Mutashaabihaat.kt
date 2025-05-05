package com.example.hifdhpartner.tests

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.ViewModel

@Composable
fun Mutashaabihaat(navController: NavController, databaseHelper: QuranDatabaseHelper, viewModel: ViewModel) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->

    }
}