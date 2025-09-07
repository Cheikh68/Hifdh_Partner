package com.example.hifdhpartner.Reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hifdhpartner.OtherScreens.BottomNavBar
import com.example.hifdhpartner.OtherScreens.MainText
import com.example.hifdhpartner.OtherScreens.MyButton
import com.example.hifdhpartner.ViewModel

@Composable
fun Reading(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            MainText("Reading")

            Spacer(modifier = Modifier.height(40.dp))

            MyButton(text = "Finish The Verse") { navController.navigate("finishreading") }
            Spacer(modifier = Modifier.height(16.dp))
            MyButton(text = "Mutashaabihaat") { navController.navigate("mutashaabreading") }
        }
    }
}

@Composable
fun BackButton(navController: NavController, viewModel: ViewModel){
    viewModel.clearMutashaabihaatSort()
    viewModel.clearFinishVerseSort()
    Column(modifier = Modifier.padding(top = 16.dp)) {
        IconButton(onClick = {
            navController.navigate("reading")
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
}