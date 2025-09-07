package com.example.hifdhpartner.Reading

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.FinishVerseQuestion
import androidx.compose.ui.unit.sp
import com.example.hifdhpartner.databases.VerseRef

@Composable
fun FinishTheVerseReading(navController: NavController, viewModel: ViewModel) {
    val filteredQuestions by viewModel.filteredFinishVerseQuestions.collectAsState()
    var currentQuestion by remember { mutableStateOf<FinishVerseQuestion?>(null) }
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
                            viewModel.filterFinishVerseToSpecificSurah(selectedId)
                        }
                    )
                    SortButton(viewModel)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredQuestions) { question ->
                        FinishVerseCard(
                            question = question,
                            onClick = {
                                showClickedCard = true
                                currentQuestion = question
                            }
                        )
                    }
                }
            }
        }

        if (showClickedCard) {
            currentQuestion?.let {
                FinishVerseReadingClickedCard(
                    question = it,
                    onDone = { showClickedCard = false }
                )
            }
        }
    }
}

@Composable
fun SortButton(viewModel: ViewModel) {
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
                    viewModel.sortFinishVerseBy("surah")
                }
            )
            DropdownMenuItem(
                text = { Text("Type") },
                onClick = {
                    selectedSortOption = "Type"
                    expanded = false
                    viewModel.sortFinishVerseBy("type")
                }
            )
            DropdownMenuItem(
                text = { Text("Known Only") },
                onClick = {
                    selectedSortOption = "Known Only"
                    expanded = false
                    viewModel.filterToKnownVersesOnly()
                }
            )
            DropdownMenuItem(
                text = { Text("Clear") },
                onClick = {
                    selectedSortOption = "Clear"
                    expanded = false
                    viewModel.clearFinishVerseSort()
                }
            )
        }
    }
}

@Composable
fun FinishVerseCard(question: FinishVerseQuestion, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Reference: surah ${question.surahId}, verse ${question.verseId}\n" +
                        "Verse Start: ${question.prompt.take(100)}...\n" +
                        "Verse End: ${question.correctAnswer}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun FinishVerseReadingClickedCard (question: FinishVerseQuestion, onDone: () -> Unit) {
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
                    text = "Reference: surah ${question.surahId}, verse ${question.verseId}\n\n" +
                            "Verse Start: ${question.prompt}...\n\n" +
                            "Verse End: ${question.correctAnswer}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onPrimary
                )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahSpecificSortButton(viewModel: ViewModel, onSurahSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value ="Select Surah to sort",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Surah to sort") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .width(180.dp)
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