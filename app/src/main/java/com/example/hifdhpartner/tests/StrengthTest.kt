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
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.databases.UserData
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.Verse
import com.example.hifdhpartner.databases.VerseRef


@Composable
fun StrengthTest(navController: NavController, viewModel: ViewModel) {
    var currentSurah by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var currentVerseRef by remember { mutableStateOf(VerseRef(0, 0)) }
    var selectedSurah by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Load a random verse on first launch
    LaunchedEffect(Unit) {
        viewModel.getRandomKnownVerse()?.let { (surah, verse) ->
            currentVerseRef = VerseRef(surah.id, verse.id)
            currentSurah = surah.id to verse.text
            selectedSurah = null
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            MainText("Strength Test")
            Spacer(modifier = Modifier.height(16.dp))

            if (currentSurah != null) {
                Text(
                    text = "From which chapter is this verse from?",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
                    Text(
                        text = "\"${currentSurah!!.second}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, lineHeight = 45.sp),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SurahDropdownMenu(
                    viewModel = viewModel,
                    selectedSurah = selectedSurah,
                    onSurahSelected = { selectedId ->
                        selectedSurah = selectedId
                        val result = viewModel.isCorrectAnswer(correct = currentVerseRef, userAnswerSurahId = selectedId)
                        if (result) {
                            viewModel.updateQuestionStats(true)
                            Toast.makeText(context, "Correct! 🎉", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateQuestionStats(false)
                            Toast.makeText(context, "Wrong! Try again.", Toast.LENGTH_SHORT).show()
                            return@SurahDropdownMenu
                        }

                        // Load next question
                        viewModel.getRandomKnownVerse()?.let { (surah, verse) ->
                            currentVerseRef = VerseRef(surah.id, verse.id)
                            currentSurah = surah.id to verse.text
                            selectedSurah = null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.getRandomKnownVerse()?.let { (surah, verse) ->
                            currentVerseRef = VerseRef(surah.id, verse.id)
                            currentSurah = surah.id to verse.text
                            selectedSurah = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.onPrimary)
                }

            } else {
                Text(
                    text = "You have no known verses. Mark some to start the quiz.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDropdownMenu(
    viewModel: ViewModel,
    selectedSurah: Int?,
    onSurahSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSurah?.let { surahId ->
                // Show selected surah's transliteration name
                viewModel.getSurahById(surahId)?.transliteration ?: "Select Surah"
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
            viewModel.getAllSurahs().forEach { surah ->
                DropdownMenuItem(
                    text = { Text("${surah.id} - ${surah.transliteration} (${surah.name})") },
                    onClick = {
                        onSurahSelected(surah.id) // send surah.id directly
                        expanded = false
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
        }
    }
}