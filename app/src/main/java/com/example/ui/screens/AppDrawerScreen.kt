package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppIconView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDrawerScreen(
    allApps: List<AppItem>,
    settings: LauncherSettings,
    onBackToHome: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAddAppToHome: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showMoreMenu by remember { mutableStateOf(false) }

    var selectedAppForSheet by remember { mutableStateOf<AppItem?>(null) }

    val filterOptions = listOf("All", "Favorites", "Recent", "Social", "Games", "Media", "Work", "Tools")

    val visibleApps = remember(allApps, searchQuery, selectedFilter) {
        allApps
            .filterNot { it.isHidden }
            .filter { app ->
                if (searchQuery.isBlank()) true
                else app.label.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
            }
            .filter { app ->
                when (selectedFilter) {
                    "All" -> true
                    "Favorites" -> app.isFavorite
                    "Recent" -> true // ordered by installTime if filtered
                    else -> app.category.equals(selectedFilter, ignoreCase = true)
                }
            }
            .let { list ->
                if (selectedFilter == "Recent") list.sortedByDescending { it.installTime }
                else list.sortedBy { it.label.lowercase(Locale.getDefault()) }
            }
    }

    var dragAccumulatorY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle pill (swipe down to return Home)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragAccumulatorY = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatorY += dragAmount.y
                            },
                            onDragEnd = {
                                if (dragAccumulatorY > 50f) {
                                    onBackToHome()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x66BAC7E5))
                )
            }

            // Header: Back, Search Field, Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragAccumulatorY = 0f },
                            onDrag = { change, dragAmount ->
                                dragAccumulatorY += dragAmount.y
                                if (dragAccumulatorY > 30f) change.consume()
                            },
                            onDragEnd = {
                                if (dragAccumulatorY > 60f) {
                                    onBackToHome()
                                }
                            }
                        )
                    },
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
                        contentDescription = "Back to Home",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${allApps.size} apps...", color = Color(0x99BAC7E5), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = Color(0x4010162B),
                        unfocusedContainerColor = Color(0x3310162B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("drawer_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x33141B33))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Launcher Settings") },
                            onClick = {
                                showMoreMenu = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }

            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) NeonCyan else Color(0x33121A33))
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else Color(0x33FFFFFF),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color(0xFF090D1A) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main App Grid
            if (visibleApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No applications found.",
                        color = Color(0x99BAC7E5),
                        fontSize = 15.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        maxItemsInEachRow = settings.columns.coerceIn(4, 7)
                    ) {
                        visibleApps.forEach { app ->
                            AppIconView(
                                app = app,
                                settings = settings,
                                onClick = { onLaunchApp(app.packageName) },
                                onLongClick = { selectedAppForSheet = app },
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // App Options Sheet
    if (selectedAppForSheet != null) {
        val app = selectedAppForSheet!!
        ModalBottomSheet(
            onDismissRequest = { selectedAppForSheet = null },
            containerColor = Color(0xFF0F1528)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    AppIconView(
                        app = app,
                        settings = settings.copy(iconSize = 48f, showLabels = false),
                        onClick = {}
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.label,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${app.category} • ${app.packageName}",
                            color = Color(0x99BAC7E5),
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onAddAppToHome(app.packageName)
                        selectedAppForSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to Home Screen", color = NeonCyan, fontSize = 15.sp)
                }

                TextButton(
                    onClick = {
                        onToggleFavorite(app.packageName)
                        selectedAppForSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (app.isFavorite) "Remove from Favorites" else "Mark as Favorite",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                TextButton(
                    onClick = {
                        onHideApp(app.packageName)
                        selectedAppForSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hide Application", color = NeonPink, fontSize = 15.sp)
                }

                TextButton(
                    onClick = {
                        onAppInfo(app.packageName)
                        selectedAppForSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("App Info", color = Color.White, fontSize = 15.sp)
                }

                TextButton(
                    onClick = {
                        onUninstallApp(app.packageName)
                        selectedAppForSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Uninstall Application", color = Color(0xFFFF5252), fontSize = 15.sp)
                }
            }
        }
    }
}
