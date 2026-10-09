package com.example.ui.screens

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.data.model.WidgetType
import com.example.ui.components.WidgetContainer
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay
import kotlinx.coroutines.launch

@Composable
fun WidgetGalleryScreen(
    settings: LauncherSettings,
    onBackToHome: () -> Unit,
    onAddWidgetToHome: (WidgetType) -> Unit,
    onAddSystemWidgetToHome: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val appWidgetHost = remember { AppWidgetHost(context, 1024) }

    val widgetPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val widgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (widgetId != -1) {
            onAddSystemWidgetToHome(widgetId)
            scope.launch {
                snackbarHostState.showSnackbar("Added Android System Widget #$widgetId")
            }
        }
    }

    val availableWidgets = listOf(
        Triple(WidgetType.CLOCK, "Liquid Glass Clock", "Live ticking neon digital clock with time zone & date"),
        Triple(WidgetType.WEATHER, "Atmospheric Weather", "Real temperature with condition and forecast"),
        Triple(WidgetType.BATTERY, "Power & Battery", "Circular gauge with charging status and health"),
        Triple(WidgetType.CALENDAR, "Calendar & Events", "Day of the month and upcoming schedule"),
        Triple(WidgetType.MUSIC, "Media Player", "Liquid Glass mini player with playback controls"),
        Triple(WidgetType.DEVICE_INFO, "System Telemetry", "Real storage and RAM diagnostic metrics")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                Spacer(modifier = Modifier.padding(8.dp))
                Column {
                    Text(
                        text = "Widget Gallery",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Liquid Glass & Android System widgets",
                        color = Color(0x99BAC7E5),
                        fontSize = 12.sp
                    )
                }
            }

            // Android System Widget Picker Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .adaptiveGlassOrClay(settings.uiMode, 20.dp, 0.35f, NeonViolet)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Android System Widgets",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose from installed apps' system widgets",
                            color = Color(0x99BAC7E5),
                            fontSize = 11.sp
                        )
                    }
                    ElevatedButton(
                        onClick = {
                            try {
                                val widgetId = appWidgetHost.allocateAppWidgetId()
                                val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                }
                                widgetPickLauncher.launch(pickIntent)
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("System widget picker not available on this device")
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = NeonViolet,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = "Pick", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Widget", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(availableWidgets) { (widgetType, title, desc) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = desc,
                                    color = Color(0x99BAC7E5),
                                    fontSize = 11.sp
                                )
                            }
                            ElevatedButton(
                                onClick = {
                                    onAddWidgetToHome(widgetType)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added $title to Home Screen")
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = NeonCyan,
                                    contentColor = Color(0xFF070B17)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        WidgetContainer(
                            widgetType = widgetType,
                            settings = settings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
