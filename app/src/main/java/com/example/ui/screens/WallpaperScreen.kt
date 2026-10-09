package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay

@Composable
fun WallpaperScreen(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("BUILT-IN") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateSettings {
                it.copy(
                    wallpaperType = "GALLERY",
                    wallpaperUri = uri.toString()
                )
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
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
                    text = "Wallpapers & Depth",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Liquid Glass, Spatial Clay, and Custom Art",
                    color = Color(0x99BAC7E5),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("BUILT-IN", "GRADIENT", "GALLERY", "SOLID").forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeonCyan else Color(0x22121A33))
                        .clickable { selectedTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color(0xFF070B17) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // WALLPAPER SELECTION CARDS
        when (selectedTab) {
            "BUILT-IN" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Default Liquid Glass Wallpaper
                    WallpaperPreviewCard(
                        title = "Liquid Glass 27",
                        isSelected = settings.wallpaperType == "BUILTIN_GLASS",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "BUILTIN_GLASS") } }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wallpaper_default),
                            contentDescription = "Liquid Glass Wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Clay 3D Space Wallpaper
                    WallpaperPreviewCard(
                        title = "Clay Spheres",
                        isSelected = settings.wallpaperType == "BUILTIN_CLAY",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "BUILTIN_CLAY") } }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wallpaper_clay),
                            contentDescription = "Clay Wallpaper",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            "GRADIENT" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WallpaperPreviewCard(
                        title = "Cyan Aura",
                        isSelected = settings.wallpaperType == "GRADIENT_AURA",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "GRADIENT_AURA") } }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF060914), Color(0xFF0E1A38), Color(0xFF003B5C))
                                    )
                                )
                        )
                    }

                    WallpaperPreviewCard(
                        title = "Violet Sunset",
                        isSelected = settings.wallpaperType == "GRADIENT_SUNSET",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "GRADIENT_SUNSET") } }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF080614), Color(0xFF26103D), Color(0xFF4A103D))
                                    )
                                )
                        )
                    }
                }
            }
            "GALLERY" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Gallery",
                            tint = NeonCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (settings.wallpaperUri != null) "Change Gallery Image" else "Choose from Device Gallery",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero-permission Android Photo Picker",
                            color = Color(0x99BAC7E5),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            "SOLID" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WallpaperPreviewCard(
                        title = "Obsidian Space",
                        isSelected = settings.wallpaperType == "SOLID_OBSIDIAN",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "SOLID_OBSIDIAN") } }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF060814))
                        )
                    }

                    WallpaperPreviewCard(
                        title = "Navy Glass",
                        isSelected = settings.wallpaperType == "SOLID_NAVY",
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateSettings { it.copy(wallpaperType = "SOLID_NAVY") } }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0E1428))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WALLPAPER EFFECTS
        Text(
            text = "WALLPAPER SHADER CONTROLS",
            color = Color(0xB3BAC7E5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SliderSettingItem(
                    label = "Wallpaper Blur",
                    value = settings.wallpaperBlur,
                    valueRange = 0f..24f,
                    displayValue = "${settings.wallpaperBlur.toInt()}dp",
                    onValueChange = { onUpdateSettings { s -> s.copy(wallpaperBlur = it) } }
                )

                SliderSettingItem(
                    label = "Dark Tint Overlay",
                    value = settings.wallpaperDarkOverlay,
                    valueRange = 0f..0.7f,
                    displayValue = "${(settings.wallpaperDarkOverlay * 100).toInt()}%",
                    onValueChange = { onUpdateSettings { s -> s.copy(wallpaperDarkOverlay = it) } }
                )

                // Depth & Spatial Effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Spatial Depth Effect", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Separates foreground widgets from background", color = Color(0x99BAC7E5), fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.wallpaperDepthEnabled,
                        onCheckedChange = { onUpdateSettings { s -> s.copy(wallpaperDepthEnabled = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun WallpaperPreviewCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    2.dp,
                    if (isSelected) NeonCyan else Color(0x33FFFFFF),
                    RoundedCornerShape(20.dp)
                )
        ) {
            content()
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(NeonCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF070B17),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = if (isSelected) NeonCyan else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
