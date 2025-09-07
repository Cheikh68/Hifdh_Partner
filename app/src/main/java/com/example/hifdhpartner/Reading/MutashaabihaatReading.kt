package com.example.hifdhpartner.Reading

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.MutashaabihaatQuestion

@Composable
fun MutashaabihaatReading(navController: NavController, viewModel: ViewModel) {
    val filteredQuestions by viewModel.filteredMutashaabihaatQuestions.collectAsState()
    var currentQuestion by remember { mutableStateOf<MutashaabihaatQuestion?>(null) }
    var showClickedCard by remember { mutableStateOf(false) }

    Box {
        Scaffold(
            bottomBar = { BottomNavBar(navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(navController, viewModel)
                    SurahSpecificSortButton(
                        viewModel = viewModel,
                        onSurahSelected = { selectedId ->
                            viewModel.filterMutashaabihaatToSpecificSurah(selectedId)
                        }
                    )
                    MutashaabihaatSortButton(viewModel)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredQuestions) { question ->
                        MutashaabihaatCard(
                            question = question,
                            onClick = {
                                showClickedCard = true
                                currentQuestion = question
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (showClickedCard) {
            currentQuestion?.let {
                MutashaabihaatReadingClickedCard(
                    question = it,
                    onDone = { showClickedCard = false },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun MutashaabihaatSortButton(viewModel: ViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf("Sort") }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedSortOption)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Surah") },
                onClick = {
                    selectedSortOption = "Surah"
                    expanded = false
                    viewModel.sortMutashaabihaatBy("surah")
                }
            )
            DropdownMenuItem(
                text = { Text("Known Only") },
                onClick = {
                    selectedSortOption = "Known Only"
                    expanded = false
                    viewModel.filterMutashaabihaatToKnownOnly()
                }
            )
            DropdownMenuItem(
                text = { Text("Clear") },
                onClick = {
                    selectedSortOption = "Clear"
                    expanded = false
                    viewModel.clearMutashaabihaatSort()
                }
            )
        }
    }
}

@Composable
fun MutashaabihaatCard(question: MutashaabihaatQuestion, onClick: () -> Unit, viewModel: ViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clickable {
                    Log.d("FinishVerseCard", "Card clicked")
                    onClick()
                }
            ,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            question.passages.zip(question.references).forEach { (passage, ref) ->
                val surahName = viewModel.getSurahNameFromId(ref.surahId) ?: "Unknown Surah"
                val displayText = passage.take(100) + if (passage.length > 100) "..." else ""
                Text(
                    text = "• \"$displayText\" (Surah ${ref.surahId}, $surahName)",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!question.explanation.isNullOrBlank()) {
                Text(
                    text = "Explanation: ${question.explanation}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MutashaabihaatReadingClickedCard (question: MutashaabihaatQuestion, onDone: () -> Unit, viewModel: ViewModel) {
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
                question.passages.zip(question.references).forEach { (passage, ref) ->
                    val surahName = viewModel.getSurahNameFromId(ref.surahId) ?: "Unknown Surah"
                    Text(
                        text = "• \"$passage\" (Surah ${ref.surahId}, $surahName)",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!question.explanation.isNullOrBlank()) {
                    Text(
                        text = "Explanation: ${question.explanation}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDone) {
                        Text("Done")
                    }
                }
            }
        }
    }
}