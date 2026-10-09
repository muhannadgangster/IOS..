package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet

@Composable
fun LiquidGlassDock(
    dockApps: List<AppItem>,
    settings: LauncherSettings,
    badgeCounts: Map<String, Int> = emptyMap(),
    onAppClick: (String) -> Unit,
    onAppLongClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(32.dp)
    val dockAlpha = (1f - settings.dockTransparency).coerceIn(0.15f, 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = dockShape,
                    ambientColor = Color.Black.copy(alpha = 0.8f),
                    spotColor = NeonCyan.copy(alpha = 0.3f)
                )
                .clip(dockShape)
                .background(Color(0xFF0F1528).copy(alpha = dockAlpha))
                .drawBehind {
                    // Specular light reflection on top half
                    val reflectionHeight = size.height * 0.45f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = reflectionHeight
                        ),
                        size = size.copy(height = reflectionHeight)
                    )
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            NeonCyan.copy(alpha = 0.45f),
                            NeonViolet.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = dockShape
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dock Apps
                dockApps.take(settings.dockItemCount.coerceIn(4, 5)).forEach { app ->
                    val badge = badgeCounts[app.packageName] ?: 0
                    AppIconView(
                        app = app,
                        settings = settings.copy(showLabels = false),
                        badgeCount = badge,
                        onClick = { onAppClick(app.packageName) },
                        onLongClick = { onAppLongClick(app.packageName) }
                    )
                }

                // Dedicated App Drawer Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(settings.iconSize.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x33141F3C))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .clickable { onOpenDrawer() }
                        .testTag("dock_all_apps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "App Drawer",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
