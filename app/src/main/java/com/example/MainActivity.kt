package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.FolderItem
import com.example.data.model.LauncherSettings
import com.example.data.repository.NotificationRepository
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AnimationSettingsScreen
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.ControlCenterScreen
import com.example.ui.screens.DefaultLauncherScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FolderModal
import com.example.ui.screens.GesturesScreen
import com.example.ui.screens.HideAppsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeScreenSettingsScreen
import com.example.ui.screens.IconSettingsScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.LockScreenPreviewScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.screens.NotificationStyleScreen
import com.example.ui.screens.PasscodeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsMainScreen
import com.example.ui.screens.WallpaperScreen
import com.example.ui.screens.WidgetGalleryScreen
import com.example.ui.theme.MyApplicationTheme

enum class LauncherScreen {
    HOME,
    LOCK_SCREEN,
    PASSCODE_SCREEN,
    APP_DRAWER,
    UNIVERSAL_SEARCH,
    WIDGET_GALLERY,
    NOTIFICATION_CENTER,
    CONTROL_CENTER,
    SETTINGS_MAIN,
    SETTINGS_APPEARANCE,
    SETTINGS_HOME_GRID,
    SETTINGS_ICONS,
    SETTINGS_WALLPAPER,
    SETTINGS_LOCK_SCREEN,
    SETTINGS_NOTIFICATION_STYLE,
    SETTINGS_ANIMATIONS,
    SETTINGS_HIDE_APPS,
    SETTINGS_FAVORITES,
    SETTINGS_GESTURES,
    SETTINGS_DEFAULT_LAUNCHER,
    SETTINGS_ABOUT
}

class MainActivity : ComponentActivity() {

    private var currentScreenState = mutableStateOf(LauncherScreen.HOME)
    private var activeFolderState = mutableStateOf<FolderItem?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LauncherApplication
        val appRepository = app.appRepository
        val settingsRepository = app.settingsRepository
        val homeLayoutRepository = app.homeLayoutRepository

        setContent {
            val settings by settingsRepository.settings.collectAsState()
            val allApps by appRepository.installedApps.collectAsState()
            val pages by homeLayoutRepository.pages.collectAsState()
            val dockItems by homeLayoutRepository.dockItems.collectAsState()
            val folders by homeLayoutRepository.folders.collectAsState()
            val notifications by NotificationRepository.notifications.collectAsState()

            val badgeCounts = remember(notifications) {
                notifications.groupingBy { it.packageName }.eachCount()
            }

            var currentScreen by currentScreenState
            var activeFolder by activeFolderState

            LaunchedEffect(Unit) {
                if (settings.lockOnLaunch) {
                    currentScreen = LauncherScreen.LOCK_SCREEN
                }
            }

            // Back button handling: pops secondary screen or folder modal to HOME
            BackHandler(enabled = currentScreen != LauncherScreen.HOME || activeFolder != null) {
                if (activeFolder != null) {
                    activeFolder = null
                } else if (currentScreen == LauncherScreen.PASSCODE_SCREEN) {
                    currentScreen = LauncherScreen.LOCK_SCREEN
                } else if (currentScreen == LauncherScreen.LOCK_SCREEN) {
                    if (!settings.lockOnLaunch) {
                        currentScreen = LauncherScreen.HOME
                    }
                } else if (currentScreen != LauncherScreen.HOME) {
                    currentScreen = LauncherScreen.HOME
                }
            }

            MyApplicationTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Wallpaper & Ambient Background
                    WallpaperBackground(settings = settings)

                    // 2. Active Screen Animated Container
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (initialState == LauncherScreen.LOCK_SCREEN && targetState == LauncherScreen.PASSCODE_SCREEN) {
                                (slideInVertically(animationSpec = tween(350)) { it } + fadeIn(tween(250))) togetherWith
                                (slideOutVertically(animationSpec = tween(350)) { -it / 3 } + fadeOut(tween(250)))
                            } else if (initialState == LauncherScreen.PASSCODE_SCREEN && targetState == LauncherScreen.LOCK_SCREEN) {
                                (slideInVertically(animationSpec = tween(350)) { -it / 3 } + fadeIn(tween(250))) togetherWith
                                (slideOutVertically(animationSpec = tween(350)) { it } + fadeOut(tween(250)))
                            } else if ((initialState == LauncherScreen.PASSCODE_SCREEN || initialState == LauncherScreen.LOCK_SCREEN) && targetState == LauncherScreen.HOME) {
                                (scaleIn(initialScale = 0.92f, animationSpec = tween(400)) + fadeIn(tween(300))) togetherWith
                                (scaleOut(targetScale = 1.08f, animationSpec = tween(400)) + fadeOut(tween(300)))
                            } else if (initialState == LauncherScreen.HOME && (targetState == LauncherScreen.LOCK_SCREEN || targetState == LauncherScreen.PASSCODE_SCREEN)) {
                                (scaleIn(initialScale = 1.06f, animationSpec = tween(350)) + fadeIn(tween(250))) togetherWith
                                (scaleOut(targetScale = 0.94f, animationSpec = tween(350)) + fadeOut(tween(250)))
                            } else {
                                fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                            }
                        },
                        label = "screen_transition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            LauncherScreen.LOCK_SCREEN -> {
                                LockScreen(
                                    settings = settings,
                                    onSwipeUpToUnlock = { currentScreen = LauncherScreen.PASSCODE_SCREEN },
                                    onDirectUnlock = { currentScreen = LauncherScreen.HOME }
                                )
                            }
                            LauncherScreen.PASSCODE_SCREEN -> {
                                PasscodeScreen(
                                    settings = settings,
                                    onUnlocked = { currentScreen = LauncherScreen.HOME },
                                    onCancel = { currentScreen = LauncherScreen.LOCK_SCREEN }
                                )
                            }
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    pages = pages,
                                    dockPackages = dockItems,
                                    folders = folders,
                                    allApps = allApps,
                                    settings = settings,
                                    badgeCounts = badgeCounts,
                                    onLaunchApp = { pkg -> appRepository.launchApp(pkg) },
                                    onOpenFolder = { folder -> activeFolder = folder },
                                    onOpenSearch = { currentScreen = LauncherScreen.UNIVERSAL_SEARCH },
                                    onOpenAppDrawer = { currentScreen = LauncherScreen.APP_DRAWER },
                                    onOpenControlCenter = { currentScreen = LauncherScreen.CONTROL_CENTER },
                                    onOpenNotificationCenter = { currentScreen = LauncherScreen.NOTIFICATION_CENTER },
                                    onOpenSettings = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onOpenWidgetGallery = { currentScreen = LauncherScreen.WIDGET_GALLERY },
                                    onOpenWallpaper = { currentScreen = LauncherScreen.SETTINGS_WALLPAPER },
                                    onOpenLockPreview = { currentScreen = LauncherScreen.LOCK_SCREEN },
                                    onRemoveItem = { p, idx -> homeLayoutRepository.removeItem(p, idx) },
                                    onMoveItemToPage = { fromP, idx, toP -> homeLayoutRepository.moveItemToPage(fromP, idx, toP) },
                                    onAddPage = { homeLayoutRepository.addPage() },
                                    onRemovePage = { p -> homeLayoutRepository.removePage(p) },
                                    onCreateFolder = { name, p, pkgs -> homeLayoutRepository.createFolder(name, p, pkgs) },
                                    onToggleFavorite = { pkg -> appRepository.toggleFavorite(pkg) },
                                    onAppInfo = { pkg -> appRepository.openAppInfo(pkg) },
                                    onUninstallApp = { pkg -> appRepository.uninstallApp(pkg) },
                                    onToggleLockHome = {
                                        settingsRepository.updateSettings { it.copy(lockHomeScreen = !it.lockHomeScreen) }
                                    }
                                )
                            }
                            LauncherScreen.APP_DRAWER -> {
                                AppDrawerScreen(
                                    allApps = allApps,
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onLaunchApp = { pkg -> appRepository.launchApp(pkg) },
                                    onToggleFavorite = { pkg -> appRepository.toggleFavorite(pkg) },
                                    onHideApp = { pkg -> appRepository.toggleHidden(pkg) },
                                    onAppInfo = { pkg -> appRepository.openAppInfo(pkg) },
                                    onUninstallApp = { pkg -> appRepository.uninstallApp(pkg) },
                                    onOpenSettings = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onAddAppToHome = { pkg -> homeLayoutRepository.addAppToPage(0, pkg) }
                                )
                            }
                            LauncherScreen.UNIVERSAL_SEARCH -> {
                                SearchScreen(
                                    allApps = allApps,
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onLaunchApp = { pkg -> appRepository.launchApp(pkg) },
                                    onNavigateSetting = { name ->
                                        currentScreen = when (name) {
                                            "Appearance" -> LauncherScreen.SETTINGS_APPEARANCE
                                            "Home Screen" -> LauncherScreen.SETTINGS_HOME_GRID
                                            "Icons" -> LauncherScreen.SETTINGS_ICONS
                                            "Wallpaper" -> LauncherScreen.SETTINGS_WALLPAPER
                                            "Lock Screen" -> LauncherScreen.SETTINGS_LOCK_SCREEN
                                            "Control Center" -> LauncherScreen.CONTROL_CENTER
                                            "Default Launcher" -> LauncherScreen.SETTINGS_DEFAULT_LAUNCHER
                                            "About" -> LauncherScreen.SETTINGS_ABOUT
                                            else -> LauncherScreen.SETTINGS_MAIN
                                        }
                                    }
                                )
                            }
                            LauncherScreen.WIDGET_GALLERY -> {
                                WidgetGalleryScreen(
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onAddWidgetToHome = { w -> homeLayoutRepository.addWidgetToPage(0, w) },
                                    onAddSystemWidgetToHome = { id -> homeLayoutRepository.addSystemWidgetToPage(0, id) }
                                )
                            }
                            LauncherScreen.NOTIFICATION_CENTER -> {
                                NotificationCenterScreen(
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onLaunchApp = { pkg -> appRepository.launchApp(pkg) }
                                )
                            }
                            LauncherScreen.CONTROL_CENTER -> {
                                ControlCenterScreen(
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onUpdateTheme = { theme ->
                                        settingsRepository.updateSettings { it.copy(themeMode = theme) }
                                    },
                                    onOpenLockScreen = { currentScreen = LauncherScreen.LOCK_SCREEN }
                                )
                            }
                            LauncherScreen.SETTINGS_MAIN -> {
                                SettingsMainScreen(
                                    settings = settings,
                                    onBackToHome = { currentScreen = LauncherScreen.HOME },
                                    onNavigate = { route ->
                                        currentScreen = when (route) {
                                            "DEFAULT_LAUNCHER" -> LauncherScreen.SETTINGS_DEFAULT_LAUNCHER
                                            "APPEARANCE" -> LauncherScreen.SETTINGS_APPEARANCE
                                            "HOME_GRID" -> LauncherScreen.SETTINGS_HOME_GRID
                                            "ICONS" -> LauncherScreen.SETTINGS_ICONS
                                            "WALLPAPER" -> LauncherScreen.SETTINGS_WALLPAPER
                                            "WIDGETS" -> LauncherScreen.WIDGET_GALLERY
                                            "LOCK_SCREEN" -> LauncherScreen.SETTINGS_LOCK_SCREEN
                                            "NOTIFICATION_STYLE" -> LauncherScreen.SETTINGS_NOTIFICATION_STYLE
                                            "CONTROL_CENTER" -> LauncherScreen.CONTROL_CENTER
                                            "GESTURES" -> LauncherScreen.SETTINGS_GESTURES
                                            "ANIMATIONS" -> LauncherScreen.SETTINGS_ANIMATIONS
                                            "FAVORITES" -> LauncherScreen.SETTINGS_FAVORITES
                                            "HIDE_APPS" -> LauncherScreen.SETTINGS_HIDE_APPS
                                            "ABOUT" -> LauncherScreen.SETTINGS_ABOUT
                                            else -> LauncherScreen.SETTINGS_MAIN
                                        }
                                    }
                                )
                            }
                            LauncherScreen.SETTINGS_APPEARANCE -> {
                                AppearanceScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_HOME_GRID -> {
                                HomeScreenSettingsScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_ICONS -> {
                                IconSettingsScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_WALLPAPER -> {
                                WallpaperScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_LOCK_SCREEN -> {
                                LockScreenPreviewScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) },
                                    onTestLockFlow = { currentScreen = LauncherScreen.LOCK_SCREEN }
                                )
                            }
                            LauncherScreen.SETTINGS_NOTIFICATION_STYLE -> {
                                NotificationStyleScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_ANIMATIONS -> {
                                AnimationSettingsScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_HIDE_APPS -> {
                                HideAppsScreen(
                                    allApps = allApps,
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onToggleHideApp = { pkg -> appRepository.toggleHidden(pkg) }
                                )
                            }
                            LauncherScreen.SETTINGS_FAVORITES -> {
                                FavoritesScreen(
                                    allApps = allApps,
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onToggleFavorite = { pkg -> appRepository.toggleFavorite(pkg) }
                                )
                            }
                            LauncherScreen.SETTINGS_GESTURES -> {
                                GesturesScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN },
                                    onUpdateSettings = { block -> settingsRepository.updateSettings(block) }
                                )
                            }
                            LauncherScreen.SETTINGS_DEFAULT_LAUNCHER -> {
                                DefaultLauncherScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                                )
                            }
                            LauncherScreen.SETTINGS_ABOUT -> {
                                AboutScreen(
                                    settings = settings,
                                    onBack = { currentScreen = LauncherScreen.SETTINGS_MAIN }
                                )
                            }
                        }
                    }

                    // 3. Folder Modal Dialog
                    if (activeFolder != null) {
                        FolderModal(
                            folder = activeFolder!!,
                            allApps = allApps,
                            settings = settings,
                            onDismiss = { activeFolder = null },
                            onLaunchApp = { pkg -> appRepository.launchApp(pkg) },
                            onRenameFolder = { fid, newName -> homeLayoutRepository.updateFolderName(fid, newName) },
                            onDeleteFolder = { fid -> homeLayoutRepository.deleteFolder(fid) },
                            onRemoveAppFromFolder = { fid, pkg -> homeLayoutRepository.removeAppFromFolder(fid, pkg) }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Whenever user presses Home or switches back to launcher, return to HOME screen!
        currentScreenState.value = LauncherScreen.HOME
        activeFolderState.value = null
    }
}

@Composable
fun WallpaperBackground(settings: LauncherSettings) {
    Box(modifier = Modifier.fillMaxSize()) {
        val blurModifier = if (settings.wallpaperBlur > 0.5f) {
            Modifier.blur(settings.wallpaperBlur.dp)
        } else {
            Modifier
        }

        when (settings.wallpaperType) {
            "BUILTIN_GLASS" -> {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_default),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                )
            }
            "BUILTIN_CLAY" -> {
                Image(
                    painter = painterResource(id = R.drawable.wallpaper_clay),
                    contentDescription = "Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                )
            }
            "GRADIENT_AURA" -> {
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
            "GRADIENT_SUNSET" -> {
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
            "SOLID_NAVY" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0E1428))
                )
            }
            "GALLERY" -> {
                if (settings.wallpaperUri != null) {
                    AsyncImage(
                        model = settings.wallpaperUri,
                        contentDescription = "Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurModifier)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.wallpaper_default),
                        contentDescription = "Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurModifier)
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF060814))
                )
            }
        }

        // Dark tint overlay based on user setting
        if (settings.wallpaperDarkOverlay > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.wallpaperDarkOverlay.coerceIn(0f, 0.9f)))
            )
        }
    }
}
