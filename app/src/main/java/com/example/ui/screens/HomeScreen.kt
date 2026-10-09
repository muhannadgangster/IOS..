package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.FolderItem
import com.example.data.model.HomeItem
import com.example.data.model.LauncherSettings
import com.example.data.model.WidgetType
import com.example.ui.components.AppIconView
import com.example.ui.components.DynamicIslandBar
import com.example.ui.components.LiquidGlassDock
import com.example.ui.components.WidgetContainer
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    pages: List<List<HomeItem>>,
    dockPackages: List<String>,
    folders: Map<String, FolderItem>,
    allApps: List<AppItem>,
    settings: LauncherSettings,
    badgeCounts: Map<String, Int>,
    onLaunchApp: (String) -> Unit,
    onOpenFolder: (FolderItem) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWidgetGallery: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenLockPreview: () -> Unit,
    onRemoveItem: (pageIndex: Int, itemIndex: Int) -> Unit,
    onMoveItemToPage: (fromPage: Int, itemIndex: Int, toPage: Int) -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: (pageIndex: Int) -> Unit,
    onCreateFolder: (name: String, pageIndex: Int, initialPackages: List<String>) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onToggleLockHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    val appMap = remember(allApps) { allApps.associateBy { it.packageName } }

    var isEditMode by remember { mutableStateOf(false) }

    var selectedAppForSheet by remember { mutableStateOf<AppItem?>(null) }
    var selectedItemIndexForSheet by remember { mutableIntStateOf(-1) }
    var showActionSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val dockApps = remember(dockPackages, appMap) {
        dockPackages.mapNotNull { appMap[it] }
    }

    // Gentle wobbly/jiggle animation during Edit Mode
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val jiggleAngle by if (isEditMode) {
        infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(130, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "jiggle_angle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Gesture detection variables
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragY = 0f
                        totalDragX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        totalDragY += dragAmount.y
                        totalDragX += dragAmount.x
                        // Only consume if predominantly vertical gesture so horizontal pager receives swipes smoothly
                        if (abs(totalDragY) > abs(totalDragX) && abs(totalDragY) > 30f) {
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        if (abs(totalDragY) > abs(totalDragX) && abs(totalDragY) > 60f) {
                            if (totalDragY < -60f) {
                                // Swipe Up -> App Drawer or configured action
                                if (settings.gestureSwipeUp == "SEARCH") {
                                    onOpenSearch()
                                } else {
                                    onOpenAppDrawer()
                                }
                            } else if (totalDragY > 60f) {
                                // Swipe Down -> Notification Center / Control Center / Search
                                when (settings.gestureSwipeDown) {
                                    "CONTROL_CENTER" -> onOpenControlCenter()
                                    "SEARCH" -> onOpenSearch()
                                    else -> onOpenNotificationCenter()
                                }
                            }
                        }
                    }
                )
            }
            .pointerInput(settings.lockHomeScreen) {
                detectTapGestures(
                    onDoubleTap = {
                        if (settings.gestureDoubleTap == "LOCK_PREVIEW") {
                            onOpenLockPreview()
                        } else if (settings.gestureDoubleTap == "CONTROL_CENTER") {
                            onOpenControlCenter()
                        } else {
                            onOpenAppDrawer()
                        }
                    },
                    onLongPress = {
                        if (!settings.lockHomeScreen) {
                            isEditMode = !isEditMode
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Dynamic Island Top Bar or Edit Mode Toolbar
            if (isEditMode) {
                // Edit Mode Header Toolbar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Edit",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Add Widget
                            ElevatedButton(
                                onClick = onOpenWidgetGallery,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0x3300F0FF),
                                    contentColor = Color.White
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Widgets, contentDescription = "Widgets", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Widget", fontSize = 11.sp)
                            }
                            // Add Page
                            ElevatedButton(
                                onClick = {
                                    onAddPage()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pages.size)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0x339D00FF),
                                    contentColor = Color.White
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Page", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Page", fontSize = 11.sp)
                            }
                            // Wallpaper shortcut
                            ElevatedButton(
                                onClick = onOpenWallpaper,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0x3300FF88),
                                    contentColor = Color.White
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Wallpaper", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Wall", fontSize = 11.sp)
                            }
                            // Settings shortcut
                            ElevatedButton(
                                onClick = onOpenSettings,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = Color(0x33BAC7E5),
                                    contentColor = Color.White
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Config", fontSize = 11.sp)
                            }
                        }

                        // Done button
                        ElevatedButton(
                            onClick = { isEditMode = false },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF070B17)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done", modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                DynamicIslandBar(
                    batteryLevel = 88,
                    isCharging = false,
                    unreadNotificationCount = badgeCounts.values.sum(),
                    onIslandClick = onOpenControlCenter,
                    onNotificationClick = onOpenNotificationCenter,
                    onSettingsClick = onOpenSettings,
                    onLockClick = onOpenLockPreview
                )
            }

            // 2. Main Page Content (HorizontalPager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val items = pages.getOrNull(pageIndex) ?: emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Widgets in this page
                    items.forEachIndexed { itemIndex, item ->
                        when (item) {
                            is HomeItem.Widget -> {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    WidgetContainer(
                                        widgetType = item.widgetType,
                                        settings = settings,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .padding(vertical = 5.dp)
                                            .then(if (isEditMode) Modifier.rotate(jiggleAngle * 0.5f) else Modifier)
                                            .combinedClickable(
                                                onClick = {
                                                    if (!isEditMode) {
                                                        when (item.widgetType) {
                                                            WidgetType.CLOCK -> onOpenLockPreview()
                                                            WidgetType.DEVICE_INFO -> onOpenSettings()
                                                            else -> {}
                                                        }
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!settings.lockHomeScreen) {
                                                        isEditMode = true
                                                    }
                                                }
                                            )
                                    )

                                    if (isEditMode) {
                                        // Delete widget button
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 8.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(NeonPink)
                                                .clickable { onRemoveItem(pageIndex, itemIndex) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Widget",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            is HomeItem.SystemWidget -> {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .padding(vertical = 5.dp)
                                            .adaptiveGlassOrClay(settings.uiMode, settings.glassCornerRadius.dp, settings.glassTransparency, NeonCyan)
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Text("Android System Widget", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("AppWidget ID: #${item.appWidgetId}", color = NeonCyan, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Real AppWidgetHost interactive component", color = Color(0x99BAC7E5), fontSize = 11.sp)
                                        }
                                    }
                                    if (isEditMode) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 8.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(NeonPink)
                                                .clickable { onRemoveItem(pageIndex, itemIndex) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Widget",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // App Icons & Folders Grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        maxItemsInEachRow = settings.columns.coerceIn(4, 7)
                    ) {
                        items.forEachIndexed { itemIndex, item ->
                            when (item) {
                                is HomeItem.App -> {
                                    val app = appMap[item.packageName]
                                    if (app != null && !app.isHidden) {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            AppIconView(
                                                app = app,
                                                settings = settings,
                                                badgeCount = badgeCounts[app.packageName] ?: 0,
                                                onClick = {
                                                    if (!isEditMode) {
                                                        onLaunchApp(app.packageName)
                                                    } else {
                                                        selectedAppForSheet = app
                                                        selectedItemIndexForSheet = itemIndex
                                                        showActionSheet = true
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!settings.lockHomeScreen) {
                                                        isEditMode = true
                                                        selectedAppForSheet = app
                                                        selectedItemIndexForSheet = itemIndex
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                                    .then(if (isEditMode) Modifier.rotate(jiggleAngle) else Modifier)
                                            )

                                            if (isEditMode) {
                                                // Remove Icon from Home Badge
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonPink)
                                                        .clickable { onRemoveItem(pageIndex, itemIndex) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Remove,
                                                        contentDescription = "Remove from Home",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }

                                                // Move across pages controls in edit mode
                                                if (pages.size > 1) {
                                                    Row(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .offset(y = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        if (pageIndex > 0) {
                                                            Box(
                                                                contentAlignment = Alignment.Center,
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xCC00F0FF))
                                                                    .clickable {
                                                                        onMoveItemToPage(pageIndex, itemIndex, pageIndex - 1)
                                                                    }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                                    contentDescription = "Move Left",
                                                                    tint = Color(0xFF060914),
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                            }
                                                        }
                                                        if (pageIndex < pages.size - 1) {
                                                            Box(
                                                                contentAlignment = Alignment.Center,
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xCC9D00FF))
                                                                    .clickable {
                                                                        onMoveItemToPage(pageIndex, itemIndex, pageIndex + 1)
                                                                    }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                                    contentDescription = "Move Right",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                is HomeItem.Folder -> {
                                    val folder = folders[item.folderId]
                                    if (folder != null) {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            FolderThumbnailView(
                                                folder = folder,
                                                appMap = appMap,
                                                settings = settings,
                                                onClick = {
                                                    if (!isEditMode) {
                                                        onOpenFolder(folder)
                                                    } else {
                                                        onOpenFolder(folder)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!settings.lockHomeScreen) {
                                                        isEditMode = true
                                                    }
                                                },
                                                modifier = if (isEditMode) Modifier.rotate(jiggleAngle) else Modifier
                                            )

                                            if (isEditMode) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonPink)
                                                        .clickable { onRemoveItem(pageIndex, itemIndex) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Delete Folder",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                is HomeItem.Widget -> {}
                                is HomeItem.SystemWidget -> {}
                            }
                        }
                    }

                    // Empty page placeholder helper in Edit Mode
                    if (items.isEmpty() && isEditMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .adaptiveGlassOrClay(settings.uiMode, 20.dp, 0.3f, NeonCyan)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Page ${pageIndex + 1} is empty", color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (pages.size > 1) {
                                    ElevatedButton(
                                        onClick = { onRemovePage(pageIndex) },
                                        colors = ButtonDefaults.elevatedButtonColors(containerColor = NeonPink)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Page", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete Empty Page", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Page Indicators & Quick Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Universal Search pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x3310162E))
                        .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { onOpenSearch() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Search",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Synchronized Page indicator dots (supports swipe left/right)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(pagerState.pageCount) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 18.dp else 6.dp, 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan else Color(0x4DFFFFFF))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Edit Mode / Lock layout toggle
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x3310162E))
                        .clickable {
                            if (!isEditMode) {
                                isEditMode = true
                            } else {
                                isEditMode = false
                            }
                        }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit Home Screen",
                        tint = if (isEditMode) NeonCyan else Color(0x99FFFFFF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // 4. Liquid Glass Dock with All Apps button
            LiquidGlassDock(
                dockApps = dockApps,
                settings = settings,
                badgeCounts = badgeCounts,
                onAppClick = onLaunchApp,
                onAppLongClick = { pkg ->
                    val app = appMap[pkg]
                    if (app != null) {
                        selectedAppForSheet = app
                        selectedItemIndexForSheet = -1
                        showActionSheet = true
                    }
                },
                onOpenDrawer = onOpenAppDrawer
            )
        }
    }

    // Context Action BottomSheet for Apps
    if (showActionSheet && selectedAppForSheet != null) {
        val app = selectedAppForSheet!!
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = sheetState,
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
                            text = app.packageName,
                            color = Color(0x99BAC7E5),
                            fontSize = 11.sp
                        )
                    }
                }

                if (selectedItemIndexForSheet >= 0) {
                    TextButton(
                        onClick = {
                            onRemoveItem(pagerState.currentPage, selectedItemIndexForSheet)
                            showActionSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove from Home Screen", color = NeonPink, fontSize = 15.sp)
                    }
                }

                TextButton(
                    onClick = {
                        onToggleFavorite(app.packageName)
                        showActionSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (app.isFavorite) "Remove from Favorites" else "Add to Favorites",
                        color = NeonCyan,
                        fontSize = 15.sp
                    )
                }

                TextButton(
                    onClick = {
                        onCreateFolder(
                            "Folder",
                            pagerState.currentPage,
                            listOf(app.packageName)
                        )
                        if (selectedItemIndexForSheet >= 0) {
                            onRemoveItem(pagerState.currentPage, selectedItemIndexForSheet)
                        }
                        showActionSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Folder with App", color = Color.White, fontSize = 15.sp)
                }

                TextButton(
                    onClick = {
                        onAppInfo(app.packageName)
                        showActionSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("App Info (System)", color = Color.White, fontSize = 15.sp)
                }

                TextButton(
                    onClick = {
                        onUninstallApp(app.packageName)
                        showActionSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Uninstall Application", color = Color(0xFFFF5252), fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderThumbnailView(
    folder: FolderItem,
    appMap: Map<String, AppItem>,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderApps = remember(folder.packageNames, appMap) {
        folder.packageNames.take(4).mapNotNull { appMap[it] }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("folder_${folder.id}")
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(settings.iconSize.dp)
                .adaptiveGlassOrClay(
                    uiMode = settings.uiMode,
                    cornerRadius = 18.dp,
                    transparency = 0.3f,
                    glowColor = NeonViolet
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(2) { idx ->
                        val app = folderApps.getOrNull(idx)
                        if (app != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x3300F0FF))
                            )
                        } else {
                            Box(modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(2) { idx ->
                        val app = folderApps.getOrNull(idx + 2)
                        if (app != null) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x339D00FF))
                            )
                        } else {
                            Box(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        if (settings.showLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = folder.name,
                fontSize = settings.labelSize.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
