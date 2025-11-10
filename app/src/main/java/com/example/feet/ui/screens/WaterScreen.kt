package com.example.feet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.feet.ui.GlassButton
import com.example.feet.ui.GlassCard
import com.example.feet.ui.LiquidProgressBar
import com.example.feet.ui.viewmodels.SharedViewModel

@Composable
fun WaterScreen(viewModel: SharedViewModel) {
    val waterMl by viewModel.todayWater.collectAsState()
    val progress = viewModel.getWaterProgress()
    val glassesConsumed = viewModel.getGlassesConsumed()
    val glassesGoal = viewModel.getGlassesGoal()
    val glassSize = remember { viewModel.getGlassSize() }

    var showGlassSizeDialog by remember { mutableStateOf(false) }
    var customGlassSize by remember { mutableStateOf(glassSize.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Water Intake",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Glass Size Setting
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Glass Size",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Text(
                    text = "${glassSize.toInt()} ml",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                GlassButton(
                    onClick = {
                        customGlassSize = glassSize.toString()
                        showGlassSizeDialog = true
                    },
                    text = "Change",
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        // Main Water Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Hydration",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                LiquidProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color(0xFF64B5F6),
                    waveColor = Color(0xFF90CAF9)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$glassesConsumed / $glassesGoal glasses",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${waterMl}ml / 4000ml",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Glass Controls Only (No Quick Add)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GlassButton(
                onClick = { viewModel.removeGlass() },
                text = "- Glass",
                modifier = Modifier.weight(1f),
                enabled = waterMl > 0 // Disable when water is 0
            )

            Spacer(modifier = Modifier.width(8.dp))

            GlassButton(
                onClick = { viewModel.addGlass() },
                text = "+ Glass",
                modifier = Modifier.weight(1f),
                enabled = waterMl < 4000 // Disable when goal is reached
            )
        }

        // Stats Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${viewModel.getRemainingGlasses()} glasses left",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${viewModel.getRemainingWaterMl()}ml remaining to 4L goal",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Glass Size Dialog
        if (showGlassSizeDialog) {
            GlassSizeDialog(
                currentSize = customGlassSize,
                onSizeChange = { customGlassSize = it },
                onConfirm = {
                    val newSize = customGlassSize.toFloatOrNull() ?: 250f
                    if (newSize > 0) {
                        viewModel.setGlassSize(newSize)
                    }
                    showGlassSizeDialog = false
                },
                onDismiss = { showGlassSizeDialog = false }
            )
        }
    }
}

@Composable
fun GlassSizeDialog(
    currentSize: String,
    onSizeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set Glass Size",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter the volume of one glass in milliliters",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Custom glass size input
                androidx.compose.material3.TextField(
                    value = currentSize,
                    onValueChange = onSizeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Glass size in ml",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = { onSizeChange("200") },
                        text = "200ml",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = { onSizeChange("250") },
                        text = "250ml",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = { onSizeChange("500") },
                        text = "500ml",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = onDismiss,
                        text = "Cancel",
                        modifier = Modifier.weight(1f)
                    )

                    GlassButton(
                        onClick = onConfirm,
                        text = "Save",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}