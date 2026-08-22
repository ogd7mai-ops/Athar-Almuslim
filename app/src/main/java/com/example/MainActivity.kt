package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavScreen
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AtharTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AudioViewModel
import com.example.ui.viewmodel.HisnViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val quranViewModel: QuranViewModel by viewModels()
    private val hisnViewModel: HisnViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AtharTheme {
                var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
                var showBatteryDialog by remember { mutableStateOf(false) }

                // Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                    if (fineLocationGranted || coarseLocationGranted) {
                        requestCurrentGpsLocation()
                    }
                }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        requestCurrentGpsLocation()
                    }

                    // Check for battery optimization bypass request
                    checkBatteryOptimization(this@MainActivity) {
                        showBatteryDialog = true
                    }

                    // Schedule alarm and enqueue WorkManager worker
                    com.example.receiver.PrayerAlarmReceiver.scheduleNextPrayerAlarm(this@MainActivity)
                    com.example.worker.PrayerRescheduleWorker.enqueuePeriodicWork(this@MainActivity)
                }

                if (showBatteryDialog) {
                    AlertDialog(
                        onDismissRequest = { showBatteryDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                        },
                        title = {
                            Text(
                                text = "استثناء التطبيق من تحسين البطارية 🔋",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = "لكي يعمل الأذان وتنبيهات الصلوات في موعدها الدقيق بالخلفية والجوال مغلق، يرجى السماح باستثناء تطبيق 'أثر المسلم' من قيود تحسين البطارية.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showBatteryDialog = false
                                    requestIgnoreBatteryOptimization(this@MainActivity)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyanGlow)
                            ) {
                                Text("السماح بالعمل في الخلفية", color = NeonCyanPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBatteryDialog = false }) {
                                Text("لاحقاً", color = TextSecondary)
                            }
                        },
                        containerColor = DarkSurface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                            when (screen) {
                                NavScreen.HOME -> HomeScreen(
                                    viewModel = homeViewModel,
                                    onNavigate = { currentScreen = it }
                                )
                                NavScreen.QURAN -> QuranScreen(
                                    viewModel = quranViewModel
                                )
                                NavScreen.ADHKAR -> AdhkarScreen(
                                    viewModel = hisnViewModel
                                )
                                NavScreen.AUDIO -> AudioScreen(
                                    viewModel = audioViewModel
                                )
                                NavScreen.SETTINGS -> SettingsScreen(
                                    settingsViewModel = settingsViewModel,
                                    audioViewModel = audioViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkBatteryOptimization(context: Context, onPromptNeeded: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                onPromptNeeded()
            }
        }
    }

    private fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(appSettingsIntent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        } else {
            try {
                val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appSettingsIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun requestCurrentGpsLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val cityName = com.example.util.LocationHelper.getCityAndCountryName(
                        this,
                        location.latitude,
                        location.longitude
                    )
                    homeViewModel.updateGpsCoordinates(
                        lat = location.latitude,
                        lon = location.longitude,
                        detectedCity = cityName
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
