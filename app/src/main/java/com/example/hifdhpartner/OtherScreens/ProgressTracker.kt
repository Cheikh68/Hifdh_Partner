package com.example.hifdhpartner.OtherScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.Surah
import com.example.hifdhpartner.databases.UserData
import com.example.hifdhpartner.databases.Verse
import com.example.hifdhpartner.databases.VerseRef


@Composable
fun ProgressTracker(
    navController: NavController,
    viewModel: ViewModel
) {
    Box {
        // Observe user data from the ViewModel
        val userData by viewModel.userData.collectAsState()
        var showCard by remember { mutableStateOf(false) }
        var currentVerses by remember { mutableStateOf<List<Verse>>(emptyList()) }
        var currentSurah by remember { mutableStateOf<Surah?>(null) }

        Scaffold(
            bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MainText("Progress Tracker")
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val surahs = viewModel.getAllSurahs()

                    items(surahs) { surah ->

                        SurahButton(
                            surahId = surah.id,
                            surahName = surah.transliteration,
                            surahTranslation = surah.translation,
                            surahArabic = surah.name,
                            surahState = viewModel.getSurahState(surah),
                            onClick = {
                                currentVerses = viewModel.getVersesForSurah(surah.id)
                                currentSurah = surah
                                showCard = true
                            }
                        )
                    }
                }
            }
        }

        if (showCard) {
            currentSurah?.let {
                SurahCard(
                    surah = it,
                    verses = currentVerses,
                    onDone = { showCard = false },
                    userData = userData,
                    viewModel = viewModel
                )
            }
        }
    }
}


@Composable
fun SurahButton(
    surahId: Int,
    surahName: String,
    surahTranslation: String,
    surahArabic: String,
    surahState: ViewModel.SurahState,
    onClick: () -> Unit

) {

    val stateColor = when (surahState) {
        ViewModel.SurahState.NotStarted -> Color.Gray
        ViewModel.SurahState.InProgress -> Color.White
        ViewModel.SurahState.Completed -> Color.Green
    }

    TextButton(
        onClick = { onClick() }
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
            StateCircle(stateColor = stateColor)
        }
    }
}


@Composable
fun SurahCard(
    surah: Surah,
    verses: List<Verse>,
    onDone: () -> Unit,
    userData: UserData?,
    viewModel: ViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Dim background for focus
            .clickable { onDone() } // Close when clicking outside
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.Center)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Known Verses - ${surah.transliteration}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(verses) { verse ->
                        val isKnown = userData?.knownVerses?.contains(VerseRef(surah.id, verse.id)) ?: false

                        VerseToggle(
                            verseId = verse.id,
                            isKnown = isKnown,
                            onToggle = { isChecked ->
                                if (isChecked) viewModel.addKnownVerse(surah.id, verse.id, verse.page_number)
                                else viewModel.removeKnownVerse(surah.id, verse.id, verse.page_number)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Button(onClick = { viewModel.addKnownSurah(surahId = surah.id, verses = verses) }) {
                            Text("Select All")
                        }

                        Button(onClick = { viewModel.removeKnownSurah(surahId = surah.id, verses = verses) }) {
                            Text("Unselect All")
                        }
                    }
                    Button(onClick = onDone) {
                        Text("Done")
                    }
                }
            }
        }
    }
}


@Composable
fun VerseToggle(
    verseId: Int,
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
                text = "$verseId",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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


@Composable
fun StateCircle(stateColor: Color, size: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color = stateColor, shape = CircleShape)
    )
}