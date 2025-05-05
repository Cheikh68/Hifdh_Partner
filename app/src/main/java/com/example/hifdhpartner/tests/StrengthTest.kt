package com.example.hifdhpartner.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextAlign
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.databases.UserData
import com.example.hifdhpartner.ViewModel


@Composable
fun StrengthTest(navController: NavController, databaseHelper: QuranDatabaseHelper, viewModel: ViewModel) {
    // State variables
    val userData by viewModel.userData.collectAsState()
    var currentSurah by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var selectedSurah by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Initialize a random question from known surahs
    LaunchedEffect(Unit) {
        userData?.knownSurahs?.takeIf { it.isNotEmpty() }?.let { knownSurahs ->
            loadRandomQuestion(databaseHelper, userData?.knownSurahs.orEmpty()) { surah ->
                currentSurah = surah
                selectedSurah = null
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            MainText("Strength Test")

            Spacer(modifier = Modifier.height(16.dp))

            if (currentSurah != null) {
                // Display question
                Text(
                    text = "From which chapter is this verse from?",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary // White text
                )
                Text(
                    text = currentSurah!!.second,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 33.sp, lineHeight = 45.sp),
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary // White text
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Surah Dropdown menu (show all surahs, but only ask questions from known surahs)
                SurahDropdownMenu(
                    userData = userData,
                    databaseHelper = databaseHelper,
                    selectedSurah = selectedSurah,
                    onSurahSelected = { selectedId ->
                        selectedSurah = selectedId

                        val correctSurahNumber = currentSurah!!.first

                        if (selectedId == correctSurahNumber) {
                            viewModel.updateQuestionStats(true)
                            Toast.makeText(
                                context,
                                "Correct! 🎉",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadRandomQuestion(databaseHelper, userData?.knownSurahs.orEmpty()) { surah ->
                                currentSurah = surah
                                selectedSurah = null
                            }
                        } else {
                            viewModel.updateQuestionStats(false)
                            Toast.makeText(
                                context,
                                "Wrong! Try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Skip button
                TextButton(
                    onClick = {
                        loadRandomQuestion(databaseHelper, userData?.knownSurahs.orEmpty()) { surah ->
                            currentSurah = surah
                            selectedSurah = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text ="Skip",
                        color = MaterialTheme.colorScheme.onPrimary // White text
                    )
                }
            } else {
                // Fallback if no known Surahs
                Text(
                    text = "You have no known Surahs. Add some to start the quiz.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary // White text
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDropdownMenu(
    userData: UserData?,
    databaseHelper: QuranDatabaseHelper,
    selectedSurah: Int?, // Int now
    onSurahSelected: (Int) -> Unit // Callback with Int
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSurah?.let { surahId ->
                // Show selected surah's transliteration name
                databaseHelper.getSurahById(surahId)?.transliteration ?: "Select Surah"
            } ?: "Select Surah",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Surah") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            databaseHelper.getAllSurahs().forEach { surah ->
                DropdownMenuItem(
                    text = { Text("${surah.id} - ${surah.transliteration} (${surah.name})") },
                    onClick = {
                        onSurahSelected(surah.id) // 🔥 send surah.id directly
                        expanded = false
                    }
                )
            }
        }
    }
}



fun loadRandomQuestion(
    databaseHelper: QuranDatabaseHelper,
    knownSurahs: List<Int>,
    onSurahLoaded: (Pair<Int, String>) -> Unit
) {
    if (knownSurahs.isNotEmpty()) {
        val randomSurahId = knownSurahs.random()
        val verses = databaseHelper.getVersesFromSurah(randomSurahId)
        if (verses.isNotEmpty()) {
            val randomVerse = verses.random()
            onSurahLoaded(Pair(randomSurahId, randomVerse))
        }
    }
}