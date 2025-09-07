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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.databases.FinishVerseQuestion
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.databases.QuranDatabaseHelper
import com.example.hifdhpartner.ViewModel


@Composable
fun FinishVerse(navController: NavController, viewModel: ViewModel) {
    val userData by viewModel.userData.collectAsState()
    var currentQuestion by remember { mutableStateOf<FinishVerseQuestion?>(null) }
    var options by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isOptionsRevealed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Load answer pools if not already loaded
    LaunchedEffect(Unit) {
        if (viewModel.answerPools.isEmpty()) {
            viewModel.answerPools = viewModel.loadAnswerPools(context)
        }

        // Load a random question
        val question = viewModel.getRandomFinishVerseQuestionFromKnownVerses()
        if (question != null) {
            currentQuestion = question
            options = viewModel.generateQuestionOptions(question)
            selectedOption = null
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
            MainText("Finish the Verse")
            Spacer(modifier = Modifier.height(16.dp))

            if (currentQuestion != null) {
                // Show verse prompt
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = currentQuestion!!.prompt,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 33.sp, lineHeight = 45.sp),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Right
                    )
                }

                // Reveal Options Button
                Button(onClick = {
                    isOptionsRevealed = true
                    selectedOption = null
                }) {
                    Text("Reveal Options")
                }

                if (isOptionsRevealed) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Options Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(options) { option ->
                            OptionButton(
                                option = option,
                                onClick = {
                                    val isCorrect = option == currentQuestion!!.correctAnswer
                                    viewModel.updateQuestionStats(isCorrect)
                                    Toast.makeText(
                                        context,
                                        if (isCorrect) "Correct! 🎉" else "Wrong! Try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    selectedOption = option
                                },
                                selectedOption = selectedOption,
                                correctOption = currentQuestion!!.correctAnswer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip Button
                TextButton(
                    onClick = {
                        if (viewModel.knowsVerses()) {
                            viewModel.viewModelScope.launch {
                                val newQuestion = viewModel.getRandomFinishVerseQuestionFromKnownVerses()
                                currentQuestion = newQuestion
                                options = newQuestion?.let { viewModel.generateQuestionOptions(it) } ?: emptyList()
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