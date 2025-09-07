package com.example.hifdhpartner.tests

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.databases.MutashaabihaatQuestion
import com.example.hifdhpartner.databases.VerseRef
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mutashaabihaat(navController: NavController, viewModel: ViewModel) {
    var currentQuestion by remember { mutableStateOf<MutashaabihaatQuestion?>(null) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val userSelections = remember { mutableStateMapOf<String, VerseRef?>() }
    var showResults by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadNewQuestion() {
        scope.launch {
            isLoading = true
            val question = viewModel.getRandomMutashaabihaatQuestionFromKnownVerses()
            currentQuestion = question
            showResults = false
            selectedOption = null
            userSelections.clear()
            question?.correctMatches?.keys?.forEach { userSelections[it] = null }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadNewQuestion() }

    val shuffledRefs = remember(currentQuestion) {
        currentQuestion?.references?.shuffled() ?: emptyList()
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            MainText("Mutashaabihaat matching")
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                currentQuestion != null -> {
                    currentQuestion!!.passages.forEachIndexed { passageIndex, passage ->
                        val passageKey = passageIndex.toString()
                        val selected = userSelections[passageKey]
                        val correctRef = currentQuestion!!.correctMatches[passageKey]


                        val bgColor = when {
                            showResults && selected == correctRef -> Color.Green
                            showResults && selected != null -> Color.Black
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = passage,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, lineHeight = 45.sp),
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                var expanded by remember { mutableStateOf(false) }

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = selected?.let { "Surah ${it.surahId}, Verse ${it.verseId}" } ?: "Select reference",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Reference") },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                        modifier = Modifier.menuAnchor()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        shuffledRefs.forEach { ref ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text("Surah ${ref.surahId}, Verse ${ref.verseId} (${viewModel.getSurahNameFromId(ref.surahId)})")
                                                },
                                                onClick = {
                                                    userSelections[passageKey] = ref
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showResults = true
                            currentQuestion?.let {
                                viewModel.updateMutashaabihaatStats(
                                    userSelections = userSelections.toMap(),
                                    correctMatches = currentQuestion!!.correctMatches
                                )
                            }},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Done")
                    }

                    // Skip Button
                    Button(
                        onClick = {
                            if (viewModel.knowsVerses()) {
                                loadNewQuestion()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Skip/Next")
                    }
                } else -> {
                    // No eligible questions
                    Text(
                        text = "You have no eligible questions for your known verses. Add more known verses or try again later.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
