package com.example.hifdhpartner.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.VerseRef


@Composable
fun ComprehensionTest(navController: NavController, viewModel: ViewModel) {
    var currentSurah by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var currentVerseRef by remember { mutableStateOf(VerseRef(0, 0)) }
    var selectedSurah by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Load a random verse on first launch
    LaunchedEffect(Unit) {
        viewModel.getRandomKnownVerse()?.let { (surah, verse) ->
            currentVerseRef = VerseRef(surah.id, verse.id)
            currentSurah = surah.id to verse.translation
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
            MainText("Comprehension Test")
            Spacer(modifier = Modifier.height(16.dp))

            if (currentSurah != null) {
                Text(
                    text = "From which chapter is this verse from?",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "\"${currentSurah!!.second}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, lineHeight = 45.sp),
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )

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
                            currentSurah = surah.id to verse.translation
                            selectedSurah = null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.getRandomKnownVerse()?.let { (surah, verse) ->
                            currentVerseRef = VerseRef(surah.id, verse.id)
                            currentSurah = surah.id to verse.translation
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