package com.example.hifdhpartner.OtherScreens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.hifdhpartner.ViewModel
import kotlin.math.round
import kotlin.math.roundToInt


@Composable
fun Goals(navController: NavController, viewModel: ViewModel) {
    var cycleLength by remember { mutableFloatStateOf(7f) }
    var currentPlan by remember { mutableStateOf<List<String>>(emptyList()) }
    val previousPlan by viewModel.previousPlan.collectAsState()
    var mainChunkSize by remember { mutableIntStateOf(0) }
    var remainderCount by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            MainText("Revision Goals")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select your revision cycle length (in days)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            CycleRange(
                value = cycleLength,
                onValueChange = { cycleLength = it }
            )

            Button(onClick = {
                val result = viewModel.dividePagesIntoRevisionPlan(cycleLength.toInt())
                Log.d("divided pages list", "Pages: $result")
                val (dividedPages, remainderChunk) = result.first
                mainChunkSize = result.second
                remainderCount = result.third
                currentPlan = dividedPages.map { compressToRanges(it).joinToString(", ") }
                if (remainderChunk.isNotEmpty()) {
                    currentPlan = currentPlan + listOf("Remainder: ${compressToRanges(remainderChunk).joinToString(", ")}")
                }
                viewModel.setNewPlan(currentPlan)
            }) {
                Text("Generate Plan")
            }

            DisplayPlan(plan = currentPlan, prev = previousPlan, pagesPerDay = mainChunkSize, remainder = remainderCount)
        }
    }
}


@Composable
fun CycleRange(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Slider(
            value = value,
            onValueChange = { onValueChange(it.roundToInt().toFloat()) },
            valueRange = 1f..30f,
            steps = 29, // 30 values = 29 steps
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
        Text(text = value.roundToInt().toString())
    }
}


@Composable
fun DisplayPlan(plan: List<String>, prev: List<String>, pagesPerDay: Int, remainder: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ){
        Spacer(modifier = Modifier.height(40.dp))
        if(plan.isNotEmpty()) {
            Text(
                text = "New Plan: ($pagesPerDay pages per day, remainder: $remainder)",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        plan.forEachIndexed { index, day ->
            if (day.startsWith("Remainder:")) {
                Text(text = day, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "Day ${index + 1}: $day", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if(prev.isNotEmpty()) {
            Text(
                text = "Previous Plan:",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        prev.forEachIndexed { index, day ->
            if (day.startsWith("Remainder:")) {
                Text(text = day, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = "Day ${index + 1}: $day", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}


fun compressToRanges(pages: List<Int>): List<String> {
    if (pages.isEmpty()) return emptyList()

    val ranges = mutableListOf<String>()
    var start = pages[0]
    var end = start

    for (i in 1 until pages.size) {
        if (pages[i] == end + 1) {
            end = pages[i]
        } else {
            ranges.add(if (start == end) "$start" else "$start–$end")
            start = pages[i]
            end = start
        }
    }
    ranges.add(if (start == end) "$start" else "$start–$end")
    return ranges
}