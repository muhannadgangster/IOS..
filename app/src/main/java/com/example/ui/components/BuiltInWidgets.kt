package com.example.ui.components

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.data.model.WidgetType
import com.example.ui.theme.BatteryGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WidgetContainer(
    widgetType: WidgetType,
    settings: LauncherSettings,
    modifier: Modifier = Modifier,
    onWidgetClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .adaptiveGlassOrClay(
                uiMode = settings.uiMode,
                cornerRadius = settings.glassCornerRadius.dp,
                transparency = settings.glassTransparency,
                glowColor = NeonCyan
            )
            .clickable(enabled = onWidgetClick != null) { onWidgetClick?.invoke() }
            .padding(14.dp)
    ) {
        when (widgetType) {
            WidgetType.CLOCK -> ClockWidget(settings)
            WidgetType.WEATHER -> WeatherWidget(settings)
            WidgetType.BATTERY -> BatteryWidget(settings)
            WidgetType.CALENDAR -> CalendarWidget(settings)
            WidgetType.MUSIC -> MusicWidget(settings)
            WidgetType.DEVICE_INFO -> DeviceInfoWidget(settings)
        }
    }
}

@Composable
fun ClockWidget(settings: LauncherSettings) {
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("h:mm", Locale.getDefault()) }
    val amPmFormat = remember { SimpleDateFormat("a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    val clockColor = remember(settings.clockColorHex) {
        try {
            Color(android.graphics.Color.parseColor(settings.clockColorHex))
        } catch (e: Exception) {
            NeonCyan
        }
    }

    when (settings.clockStyle) {
        "ANALOG_GLASS" -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Analog Face
                val cal = Calendar.getInstance().apply { time = currentTime }
                val hours = cal.get(Calendar.HOUR)
                val minutes = cal.get(Calendar.MINUTE)
                val seconds = cal.get(Calendar.SECOND)

                Canvas(modifier = Modifier.size(90.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Dial rim
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Hour hand
                    val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30 - 90).toDouble())
                    val hourLength = radius * 0.5f
                    drawLine(
                        color = Color.White,
                        start = center,
                        end = Offset(
                            center.x + (hourLength * cos(hourAngle)).toFloat(),
                            center.y + (hourLength * sin(hourAngle)).toFloat()
                        ),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Minute hand
                    val minAngle = Math.toRadians((minutes * 6 - 90).toDouble())
                    val minLength = radius * 0.75f
                    drawLine(
                        color = NeonCyan,
                        start = center,
                        end = Offset(
                            center.x + (minLength * cos(minAngle)).toFloat(),
                            center.y + (minLength * sin(minAngle)).toFloat()
                        ),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Second hand
                    val secAngle = Math.toRadians((seconds * 6 - 90).toDouble())
                    val secLength = radius * 0.85f
                    drawLine(
                        color = NeonPink,
                        start = center,
                        end = Offset(
                            center.x + (secLength * cos(secAngle)).toFloat(),
                            center.y + (secLength * sin(secAngle)).toFloat()
                        ),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawCircle(color = NeonPink, radius = 3.dp.toPx(), center = center)
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = timeFormat.format(currentTime),
                        color = clockColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(currentTime),
                        color = Color(0xB3BAC7E5),
                        fontSize = 11.sp
                    )
                }
            }
        }
        else -> {
            // Digital / Neon layout
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = timeFormat.format(currentTime),
                        fontSize = (settings.clockSize * 0.75f).sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp,
                        color = clockColor,
                        fontFamily = if (settings.clockFont == "MONO_FUTURISTIC") FontFamily.Monospace else FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = amPmFormat.format(currentTime).uppercase(Locale.getDefault()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonViolet,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Column {
                    Text(
                        text = dateFormat.format(currentTime),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cupertino, CA • Clean Air 28",
                            color = Color(0x99BAC7E5),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherWidget(settings: LauncherSettings) {
    var isFahrenheit by remember { mutableStateOf(false) }
    val tempC = 22
    val tempF = 72

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isFahrenheit = !isFahrenheit },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "San Francisco",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Mostly Sunny",
                    color = Color(0xB3BAC7E5),
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Weather",
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(32.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isFahrenheit) "$tempF°" else "$tempC°",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isFahrenheit) "H: 76°  L: 58°" else "H: 24°  L: 14°",
                    color = Color(0x99FFFFFF),
                    fontSize = 10.sp
                )
                Text(
                    text = "UV Index 4 • Tap for °F/°C",
                    color = NeonCyan,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun BatteryWidget(settings: LauncherSettings) {
    val context = LocalContext.current
    var batteryPercent by remember { mutableIntStateOf(85) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent ?: return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPercent = (level * 100) / scale
                }
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = if (isCharging) BatteryGreen else NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCharging) "Charging" else "Battery",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$batteryPercent%",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isCharging) "Fast charging" else "Estimated 14h left",
                color = Color(0x99BAC7E5),
                fontSize = 10.sp
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                color = Color(0x22FFFFFF),
                strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize()
            )
            CircularProgressIndicator(
                progress = { (batteryPercent / 100f).coerceIn(0f, 1f) },
                color = if (batteryPercent <= 20) Color(0xFFFF5252) else BatteryGreen,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "$batteryPercent%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CalendarWidget(settings: LauncherSettings) {
    val cal = remember { Calendar.getInstance() }
    val dayOfMonth = remember { cal.get(Calendar.DAY_OF_MONTH).toString() }
    val dayOfWeek = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time) }
    val monthName = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = NeonPink,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = monthName.uppercase(Locale.getDefault()),
                    color = NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = dayOfWeek,
                color = Color(0xB3BAC7E5),
                fontSize = 11.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = dayOfMonth,
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "No more events today",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tomorrow: Product Review 10:00 AM",
                    color = Color(0x99BAC7E5),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MusicWidget(settings: LauncherSettings) {
    var isPlaying by remember { mutableStateOf(false) }
    var trackProgress by remember { mutableFloatStateOf(0.42f) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A223E))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Album",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Starfall Horizons",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Liquid Soundscape",
                        color = Color(0xB3BAC7E5),
                        fontSize = 10.sp
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { trackProgress },
            color = NeonCyan,
            trackColor = Color(0x33FFFFFF),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { trackProgress = (trackProgress - 0.1f).coerceAtLeast(0f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .clickable { isPlaying = !isPlaying }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color(0xFF090D1A),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { trackProgress = (trackProgress + 0.1f).coerceAtMost(1f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceInfoWidget(settings: LauncherSettings) {
    val context = LocalContext.current
    val (storageFreeGb, storageTotalGb) = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val free = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
            val total = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
            free to total
        } catch (e: Exception) {
            48L to 128L
        }
    }

    val (ramFreeGb, ramTotalGb) = remember {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val free = memInfo.availMem / (1024 * 1024 * 1024f)
            val total = memInfo.totalMem / (1024 * 1024 * 1024f)
            String.format(Locale.getDefault(), "%.1f", free) to String.format(Locale.getDefault(), "%.1f", total)
        } catch (e: Exception) {
            "4.2" to "8.0"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "iOS 27 System Info",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x3300F0FF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "OPTIMIZED",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Storage
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Storage",
                        tint = NeonViolet,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Storage",
                        color = Color(0xB3BAC7E5),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${storageFreeGb}GB free / ${storageTotalGb}GB",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // RAM
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "RAM",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RAM",
                        color = Color(0xB3BAC7E5),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${ramFreeGb}GB avail / ${ramTotalGb}GB",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
