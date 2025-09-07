package com.example.hifdhpartner.OtherScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.MenuBook
import com.example.hifdhpartner.ViewModel


@Composable
fun MainScreen(navController: NavController, viewModel: ViewModel) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) } // Add Bottom Navigation Bar
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Ensures content doesn't overlap bottom bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                MainText("Hifdh Partner")

                Spacer(modifier = Modifier.height(50.dp))

                MyButton(text = "Progress tracker") { navController.navigate("progress") }
                Spacer(modifier = Modifier.height(16.dp))
                MyButton(text = "Goals") { navController.navigate("goals") }

                Spacer(modifier = Modifier.height(50.dp))

                MainText("Tests")
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Adjust height as needed
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FeatureButton("Strength test") { navController.navigate("strentest") } }
                    item { FeatureButton("Comprehension test") { navController.navigate("comptest") } }
                    item { FeatureButton("Mutashaabihaat") { navController.navigate("mutashaabtest") } }
                    item { FeatureButton("Finish the verse") {
                        viewModel.filterToKnownVersesOnly() // <-- filter first
                        navController.navigate("finishtest")
                    } }
                }
            }
        }
    }
}


@Composable
fun MainText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimary, // White text
        modifier = Modifier
            .fillMaxWidth() // Ensures the text is centered horizontally
            .padding(top = 15.dp, start = 16.dp, end = 16.dp), // Add top padding
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,  // Makes the text bold
            fontSize = 24.sp               // Adjust font size for larger text
        )
    )
}

@Composable
fun MyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary, // White text
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
fun FeatureButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("main", Icons.Default.Home, "Home"),
        BottomNavItem("reading", Icons.Filled.MenuBook, "Reading"),
        BottomNavItem("userprofile", Icons.Default.Person, "Profile"),
        BottomNavItem("settings", Icons.Default.Settings, "Settings")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label, tint = MaterialTheme.colorScheme.onSurface) },
                label = { Text(item.label, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp) },
                selected = currentRoute == item.route,
                onClick = { if(currentRoute != item.route) navController.navigate(item.route) }
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)