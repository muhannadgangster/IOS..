package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.ui.components.MusicWidget
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlCenterScreen(
    settings: LauncherSettings,
    onBackToHome: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onOpenLockScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }

    var brightnessVal by remember { mutableFloatStateOf(0.75f) }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Check Wi-Fi connectivity
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    val isWifiConnected = remember {
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    // Flashlight management
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isFlashlightOn && cameraId != null) {
                try {
                    cameraManager.setTorchMode(cameraId, false)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    fun toggleFlashlight() {
        if (cameraId != null) {
            try {
                val newState = !isFlashlightOn
                cameraManager.setTorchMode(cameraId, newState)
                isFlashlightOn = newState
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x33141B33))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Control Center",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Connectivity 2x2 Glass Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveGlassOrClay(settings.uiMode, 26.dp, 0.35f, NeonCyan)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "CONNECTIVITY",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Wi-Fi
                    ControlSquareButton(
                        icon = Icons.Default.NetworkWifi,
                        label = "Wi-Fi",
                        sub = if (isWifiConnected) "Connected" else "Available",
                        isActive = isWifiConnected,
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                    // Bluetooth
                    ControlSquareButton(
                        icon = Icons.Default.Bluetooth,
                        label = "Bluetooth",
                        sub = "Settings",
                        isActive = true,
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cellular
                    ControlSquareButton(
                        icon = Icons.Default.NetworkCell,
                        label = "Cellular",
                        sub = "Network",
                        isActive = false,
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                    // Airplane Mode
                    ControlSquareButton(
                        icon = Icons.Default.AirplanemodeActive,
                        label = "Airplane",
                        sub = "Settings",
                        isActive = false,
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Media Player Mini Tile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .adaptiveGlassOrClay(settings.uiMode, 24.dp, 0.35f, NeonViolet)
                .padding(14.dp)
        ) {
            MusicWidget(settings)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Brightness & Volume Sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Brightness Slider Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .adaptiveGlassOrClay(settings.uiMode, 24.dp, 0.35f, Color(0xFFFFD600))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Display",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = brightnessVal,
                        onValueChange = { newVal ->
                            brightnessVal = newVal
                            val act = context as? Activity
                            val lp = act?.window?.attributes
                            lp?.screenBrightness = newVal
                            act?.window?.attributes = lp
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFFFD600),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                    Text(
                        text = "${(brightnessVal * 100).toInt()}%",
                        color = Color(0x99BAC7E5),
                        fontSize = 11.sp
                    )
                }
            }

            // Volume Slider Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .adaptiveGlassOrClay(settings.uiMode, 24.dp, 0.35f, NeonCyan)
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Volume",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = currentVolume,
                        onValueChange = { newVal ->
                            currentVolume = newVal
                            val targetVol = (newVal * maxVolume).toInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )
                    Text(
                        text = "${(currentVolume * 100).toInt()}%",
                        color = Color(0x99BAC7E5),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Quick Utility Tiles Grid
        Text(
            text = "SYSTEM CONTROLS",
            color = Color(0xB3BAC7E5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            maxItemsInEachRow = 3
        ) {
            // Flashlight Tile
            QuickTileItem(
                icon = Icons.Default.FlashlightOn,
                label = "Flashlight",
                isActive = isFlashlightOn,
                activeColor = Color(0xFFFFD600),
                onClick = { toggleFlashlight() }
            )

            // Rotation Lock
            QuickTileItem(
                icon = Icons.Default.ScreenRotation,
                label = "Rotation",
                isActive = true,
                activeColor = NeonViolet,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            // Dark Mode
            QuickTileItem(
                icon = Icons.Default.DarkMode,
                label = "Dark Mode",
                isActive = settings.themeMode == "DARK",
                activeColor = NeonCyan,
                onClick = {
                    onUpdateTheme(if (settings.themeMode == "DARK") "LIGHT" else "DARK")
                }
            )

            // Battery Saver
            QuickTileItem(
                icon = Icons.Default.BatterySaver,
                label = "Battery Saver",
                isActive = false,
                activeColor = Color(0xFFFFAB00),
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            // Do Not Disturb
            QuickTileItem(
                icon = Icons.Default.DoNotDisturb,
                label = "DND Mode",
                isActive = false,
                activeColor = NeonPink,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            // Location
            QuickTileItem(
                icon = Icons.Default.LocationOn,
                label = "Location",
                isActive = true,
                activeColor = NeonCyan,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )

            // Lock Screen Tile
            QuickTileItem(
                icon = Icons.Default.Lock,
                label = "Lock Screen",
                isActive = true,
                activeColor = NeonPink,
                onClick = {
                    onOpenLockScreen()
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ControlSquareButton(
    icon: ImageVector,
    label: String,
    sub: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isActive) Color(0x3300F0FF) else Color(0x22131C35))
            .border(
                1.dp,
                if (isActive) NeonCyan.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isActive) NeonCyan else Color(0x33FFFFFF))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF090D1C) else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = sub,
                color = if (isActive) NeonCyan else Color(0x88BAC7E5),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun QuickTileItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isActive) activeColor.copy(alpha = 0.25f) else Color(0x22121A33))
                .border(
                    1.dp,
                    if (isActive) activeColor.copy(alpha = 0.8f) else Color(0x22FFFFFF),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
