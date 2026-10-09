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
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay

@Composable
fun AppearanceScreen(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
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
                    text = "Appearance & Shaders",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Liquid Glass & Clay UI Engine",
                    color = Color(0x99BAC7E5),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LIVE PREVIEW CARD
        Text(
            text = "LIVE SHADER PREVIEW",
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .adaptiveGlassOrClay(
                    uiMode = settings.uiMode,
                    cornerRadius = settings.glassCornerRadius.dp,
                    transparency = settings.glassTransparency,
                    glowColor = NeonCyan
                )
                .padding(20.dp)
        ) {
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
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Liquid",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liquid Glass Shader",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x3300F0FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = settings.uiMode,
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Dynamic specular refraction with ${settings.glassCornerRadius.toInt()}dp radius and ${(settings.glassTransparency * 100).toInt()}% transparency.",
                    color = Color(0xCCBAC7E5),
                    fontSize = 12.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonViolet)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UI MODE SELECTOR: Glass / Clay / Mixed
        Text(
            text = "SURFACE MODE",
            color = Color(0xB3BAC7E5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("GLASS" to "Liquid Glass", "CLAY" to "Clay UI", "MIXED" to "Hybrid").forEach { (mode, label) ->
                val isSelected = settings.uiMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonCyan else Color(0x22121A33))
                        .clickable { onUpdateSettings { it.copy(uiMode = mode) } }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF070B17) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REAL SLIDERS SECTION
        Text(
            text = "SHADER PARAMETERS",
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
                // Transparency Slider
                SliderSettingItem(
                    label = "Glass Transparency",
                    value = settings.glassTransparency,
                    valueRange = 0.1f..0.85f,
                    displayValue = "${(settings.glassTransparency * 100).toInt()}%",
                    onValueChange = { newVal -> onUpdateSettings { it.copy(glassTransparency = newVal) } }
                )

                // Blur Intensity
                SliderSettingItem(
                    label = "Blur Intensity",
                    value = settings.glassBlurIntensity,
                    valueRange = 4f..32f,
                    displayValue = "${settings.glassBlurIntensity.toInt()}dp",
                    onValueChange = { newVal -> onUpdateSettings { it.copy(glassBlurIntensity = newVal) } }
                )

                // Reflection Intensity
                SliderSettingItem(
                    label = "Specular Reflection",
                    value = settings.glassReflection,
                    valueRange = 0.0f..1.0f,
                    displayValue = "${(settings.glassReflection * 100).toInt()}%",
                    onValueChange = { newVal -> onUpdateSettings { it.copy(glassReflection = newVal) } }
                )

                // Glow Intensity
                SliderSettingItem(
                    label = "Neon Rim Glow",
                    value = settings.glassGlowIntensity,
                    valueRange = 0.0f..1.0f,
                    displayValue = "${(settings.glassGlowIntensity * 100).toInt()}%",
                    onValueChange = { newVal -> onUpdateSettings { it.copy(glassGlowIntensity = newVal) } }
                )

                // Corner Radius
                SliderSettingItem(
                    label = "Corner Radius",
                    value = settings.glassCornerRadius,
                    valueRange = 12f..36f,
                    displayValue = "${settings.glassCornerRadius.toInt()}dp",
                    onValueChange = { newVal -> onUpdateSettings { it.copy(glassCornerRadius = newVal) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun SliderSettingItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = displayValue, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
