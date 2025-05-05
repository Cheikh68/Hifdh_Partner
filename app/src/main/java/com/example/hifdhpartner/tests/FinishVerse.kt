package com.example.hifdhpartner.tests

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.databases.FinishVerseQuestion
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.ViewModel


@Composable
fun FinishVerse(navController: NavController, databaseHelper: QuranDatabaseHelper, viewModel: ViewModel) {
    val userData by viewModel.userData.collectAsState()
    var currentQuestion by remember { mutableStateOf<FinishVerseQuestion?>(null) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isOptionsRevealed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Initialize a random question from known surahs
    LaunchedEffect(Unit) {
        userData?.knownSurahs?.takeIf { it.isNotEmpty() }?.let { knownSurahs ->
            val randomQuestion = viewModel.getRandomFinishVerseQuestion(knownSurahs)
            if (randomQuestion != null) {
                currentQuestion = randomQuestion
                options = viewModel.generateQuestionOptions(randomQuestion) // This should generate the 4 options
                selectedOption = null
            }
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
        ) {
            // Main text
            MainText("Finish the Verse")

            Spacer(modifier = Modifier.height(16.dp))

            // Check if a question has been loaded
            if (currentQuestion != null) {
                // Display the prompt (the start of the verse)
                Text(
                    text = currentQuestion!!.prompt,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 33.sp, lineHeight = 45.sp),
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                // Reveal options button
                Button(onClick = { isOptionsRevealed = true }) {
                    Text("Reveal Options")
                }

                // Show options if revealed
                if (isOptionsRevealed) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Display the options in a grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),  // This will create 2 columns
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(options) { option ->
                            OptionButton(
                                option = option,
                                onClick = {
                                    val isCorrect = option == currentQuestion!!.correctAnswer
                                    if (isCorrect) {
                                        viewModel.updateQuestionStats(true)
                                        Toast.makeText(context, "Correct! 🎉", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateQuestionStats(false)
                                        Toast.makeText(context, "Wrong! Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                    // Update the button background color based on correctness
                                    selectedOption = option
                                },
                                selectedOption = selectedOption,
                                correctOption = currentQuestion!!.correctAnswer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip button
                TextButton(
                    onClick = {
                        // Launch a coroutine to fetch a random question
                        userData?.knownSurahs?.takeIf { it.isNotEmpty() }?.let { knownSurahs ->
                            // Launch a coroutine to call suspend function
                            viewModel.viewModelScope.launch {
                                val randomQuestion = viewModel.getRandomFinishVerseQuestion(knownSurahs)
                                currentQuestion = randomQuestion
                                options = randomQuestion?.let { viewModel.generateQuestionOptions(it) } ?: emptyList()
                                selectedOption = null
                                isOptionsRevealed = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip/Next",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Fallback if no known Surahs
                Text(
                    text = "You have no known Surahs. Add some to start the quiz.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun OptionButton(
    option: String,
    onClick: () -> Unit,
    selectedOption: String?,
    correctOption: String
) {
    val backgroundColor = when {
        option == correctOption && option == selectedOption -> Color.Green
        option != correctOption && option == selectedOption -> Color.Black
        else -> Color.Gray
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
    ) {
        Text(text = option, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontSize = 23.sp, lineHeight = 30.sp))
    }
}