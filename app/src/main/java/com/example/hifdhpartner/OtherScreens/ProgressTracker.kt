package com.example.hifdhpartner.OtherScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.QuranDatabaseHelper


@Composable
fun ProgressTracker(
    navController: NavController,
    databaseHelper: QuranDatabaseHelper,
    viewModel: ViewModel
) {
    // Observe user data from the ViewModel
    val userData by viewModel.userData.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            MainText("Progress Tracker")
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val surahs = databaseHelper.getAllSurahs()

                items(surahs) { surah ->
                    // Dynamically bind the toggle state based on `userData.knownSurahs`
                    val isKnown = userData?.knownSurahs?.contains(surah.id) ?: false

                    SurahToggle(
                        surahId = surah.id,
                        surahName = surah.transliteration,
                        surahTranslation = surah.translation,
                        surahArabic = surah.name,
                        isKnown = isKnown,
                        onToggle = { isChecked ->
                            if (isChecked) {
                                viewModel.addKnownSurah(surah.id)
                            } else {
                                viewModel.removeKnownSurah(surah.id)
                            }
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun SurahToggle(
    surahId: Int,
    surahName: String,
    surahTranslation: String,
    surahArabic: String,
    isKnown: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$surahId - $surahName ($surahTranslation)",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary // White text
            )
            Text(
                text = surahArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 28.sp // Adjust font size for Arabic text
                ),
                color = MaterialTheme.colorScheme.onPrimary // White text
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        }

        Switch(
            checked = isKnown,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.Gray,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}




