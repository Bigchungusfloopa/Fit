package com.example.feet.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.feet.ui.GlassCard
import com.example.feet.ui.viewmodels.SharedViewModel

// Define our tab items
sealed class Screen(val route: String, val title: String, val icon: String) {
    object Water : Screen("water", "Water", "💧")
    object Steps : Screen("steps", "Steps", "👣")
    object Workout : Screen("workout", "Workout", "💪")
}

@Composable
fun MainScreen(viewModel: SharedViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        Screen.Water,
        Screen.Steps,
        Screen.Workout
    )

    Scaffold(
        bottomBar = {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(70.dp)
                ) {
                    tabs.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = {
                                Text(
                                    text = screen.icon,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> WaterScreen(viewModel)
                1 -> StepsScreen(viewModel)
                2 -> WorkoutScreen(viewModel)
            }
        }
    }
}