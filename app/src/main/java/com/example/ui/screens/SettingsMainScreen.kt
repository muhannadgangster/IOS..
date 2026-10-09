package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay

@Composable
fun SettingsMainScreen(
    settings: LauncherSettings,
    onBackToHome: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
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
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "iOS 27 by mun. Configuration",
                    color = Color(0x99BAC7E5),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Default Launcher Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
                .clickable { onNavigate("DEFAULT_LAUNCHER") }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Default",
                            tint = Color(0xFF070B17),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Default Launcher",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Set as Default Home Application",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open",
                    tint = Color(0x66FFFFFF),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // DESIGN & APPEARANCE SECTION
        SettingsSectionHeader("DESIGN & APPEARANCE")
        SettingsRowCard(settings, listOf(
            SettingsItem("Appearance", "Liquid Glass, Clay UI, Transparency, Blur", Icons.Default.ColorLens, NeonCyan) { onNavigate("APPEARANCE") },
            SettingsItem("Home Screen", "Grid size, Icon sizes, Spacing, Dock", Icons.Default.GridOn, NeonViolet) { onNavigate("HOME_GRID") },
            SettingsItem("Icon Customization", "Styles, Squircle shapes, Glow & Rim", Icons.Default.Dashboard, NeonPink) { onNavigate("ICONS") },
            SettingsItem("Wallpaper", "Liquid Glass, Clay, Gradients, Gallery", Icons.Default.Image, ElectricBlue) { onNavigate("WALLPAPER") }
        ))

        Spacer(modifier = Modifier.height(16.dp))

        // SYSTEM & SURFACES SECTION
        SettingsSectionHeader("SYSTEM & SURFACES")
        SettingsRowCard(settings, listOf(
            SettingsItem("Widgets", "Clock, Weather, Battery, Calendar gallery", Icons.Default.Widgets, NeonCyan) { onNavigate("WIDGETS") },
            SettingsItem("Lock Screen", "Futuristic clock formats, Fonts, Preview", Icons.Default.Lock, NeonViolet) { onNavigate("LOCK_SCREEN") },
            SettingsItem("Notification Style", "Badges, iOS Glass cards, Spacing", Icons.Default.Notifications, NeonPink) { onNavigate("NOTIFICATION_STYLE") },
            SettingsItem("Control Center", "Liquid Glass quick toggles, Sliders", Icons.Default.Tune, ElectricBlue) { onNavigate("CONTROL_CENTER") }
        ))

        Spacer(modifier = Modifier.height(16.dp))

        // BEHAVIOR & INTERACTION
        SettingsSectionHeader("BEHAVIOR & GESTURES")
        SettingsRowCard(settings, listOf(
            SettingsItem("Gestures", "Swipe up, Swipe down, Double tap shortcuts", Icons.Default.Gesture, NeonCyan) { onNavigate("GESTURES") },
            SettingsItem("Animations", "Smooth, Cinematic, Fast velocity presets", Icons.Default.Animation, NeonViolet) { onNavigate("ANIMATIONS") },
            SettingsItem("Favorite Apps", "Configure primary favorites and dock", Icons.Default.Star, Color(0xFFFFD600)) { onNavigate("FAVORITES") },
            SettingsItem("Hide Apps", "Remove private apps from drawer & search", Icons.Default.VisibilityOff, NeonPink) { onNavigate("HIDE_APPS") }
        ))

        Spacer(modifier = Modifier.height(16.dp))

        // ABOUT & PLATFORM
        SettingsSectionHeader("PLATFORM")
        SettingsRowCard(settings, listOf(
            SettingsItem("About iOS 27 by mun.", "Version 27.0 • Liquid Engine Build 2026", Icons.Default.Info, Color.White) { onNavigate("ABOUT") }
        ))

        Spacer(modifier = Modifier.height(60.dp))
    }
}

data class SettingsItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val onClick: () -> Unit
)

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xB3BAC7E5),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsRowCard(
    settings: LauncherSettings,
    items: List<SettingsItem>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
            .padding(vertical = 4.dp)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.iconTint.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.description,
                                color = Color(0x99BAC7E5),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next",
                        tint = Color(0x4DFFFFFF),
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(0.5.dp)
                            .background(Color(0x1AFFFFFF))
                    )
                }
            }
        }
    }
}
