package com.example.feet

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.feet.services.StepTrackerService
import com.example.feet.ui.GlassButton
import com.example.feet.ui.screens.MainScreen
import com.example.feet.ui.theme.FeetTheme
import com.example.feet.ui.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel = SharedViewModel()
    private var stepTrackerService: StepTrackerService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StepTrackerService.StepTrackerBinder
            stepTrackerService = binder.getService()
            isServiceBound = true

            // Connect the service to the viewModel
            lifecycleScope.launch {
                stepTrackerService?.stepCount?.collect { steps ->
                    viewModel.updateLiveSteps(steps)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stepTrackerService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if device has step sensors
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val hasStepSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) != null ||
                sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR) != null

        viewModel.setStepSensorAvailable(hasStepSensor)

        setContent {
            FeetTheme {
                var hasStepPermission by remember { mutableStateOf(false) }
                var showPermissionRationale by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasStepPermission = isGranted
                    if (isGranted && hasStepSensor) {
                        startStepTrackerService()
                    } else if (!isGranted) {
                        showPermissionRationale = true
                    }
                }

                // Check and request permission
                LaunchedEffect(Unit) {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Manifest.permission.ACTIVITY_RECOGNITION
                    } else {
                        // For older versions, no permission needed
                        null
                    }

                    if (permission != null) {
                        when {
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                hasStepPermission = true
                                if (hasStepSensor) {
                                    startStepTrackerService()
                                }
                            }
                            else -> {
                                // Request permission only if we have sensors
                                if (hasStepSensor) {
                                    permissionLauncher.launch(permission)
                                } else {
                                    // No sensors, skip permission
                                    hasStepPermission = true
                                }
                            }
                        }
                    } else {
                        // For Android < 10, no permission needed
                        hasStepPermission = true
                        if (hasStepSensor) {
                            startStepTrackerService()
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasStepPermission) {
                        MainScreen(viewModel = viewModel)
                    } else if (showPermissionRationale) {
                        // Show permission rationale screen
                        PermissionRationaleScreen {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    } else {
                        // Show loading or initial screen
                        LoadingScreen()
                    }
                }
            }
        }
    }

    private fun startStepTrackerService() {
        if (!isServiceBound) {
            val serviceIntent = Intent(this, StepTrackerService::class.java)
            startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        // Optionally stop the service completely
        // stopService(Intent(this, StepTrackerService::class.java))
    }
}

// Permission Rationale Screen
@Composable
fun PermissionRationaleScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step Tracking Required",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "To provide accurate step tracking and fitness insights, Feet needs permission to access your step count.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "• Track your steps automatically\n• Monitor your daily progress\n• Provide accurate fitness data",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlassButton(
            onClick = onRequestPermission,
            text = "Allow Step Tracking",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You can change this permission anytime in Settings",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

// Loading Screen
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Feet",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Setting up your fitness tracker...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}