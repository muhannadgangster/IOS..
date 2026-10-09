package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ios27_launcher_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private fun loadSettings(): LauncherSettings {
        return LauncherSettings(
            uiMode = prefs.getString("uiMode", "GLASS") ?: "GLASS",
            themeMode = prefs.getString("themeMode", "DARK") ?: "DARK",
            glassTransparency = prefs.getFloat("glassTransparency", 0.35f),
            glassBlurIntensity = prefs.getFloat("glassBlurIntensity", 16f),
            glassBrightness = prefs.getFloat("glassBrightness", 1.0f),
            glassReflection = prefs.getFloat("glassReflection", 0.7f),
            glassGlowIntensity = prefs.getFloat("glassGlowIntensity", 0.6f),
            glassCornerRadius = prefs.getFloat("glassCornerRadius", 22f),
            clayShadowDepth = prefs.getFloat("clayShadowDepth", 8f),

            rows = prefs.getInt("rows", 5),
            columns = prefs.getInt("columns", 4),
            iconSize = prefs.getFloat("iconSize", 58f),
            iconShape = prefs.getString("iconShape", "SQUIRCLE") ?: "SQUIRCLE",
            iconPackStyle = prefs.getString("iconPackStyle", "IOS_GLASS") ?: "IOS_GLASS",
            showLabels = prefs.getBoolean("showLabels", true),
            labelSize = prefs.getFloat("labelSize", 12f),
            dockItemCount = prefs.getInt("dockItemCount", 4),
            dockTransparency = prefs.getFloat("dockTransparency", 0.35f),
            lockHomeScreen = prefs.getBoolean("lockHomeScreen", false),

            wallpaperType = prefs.getString("wallpaperType", "BUILTIN_GLASS") ?: "BUILTIN_GLASS",
            wallpaperUri = prefs.getString("wallpaperUri", null),
            wallpaperBlur = prefs.getFloat("wallpaperBlur", 0f),
            wallpaperBrightness = prefs.getFloat("wallpaperBrightness", 1.0f),
            wallpaperDarkOverlay = prefs.getFloat("wallpaperDarkOverlay", 0.15f),
            wallpaperDepthEnabled = prefs.getBoolean("wallpaperDepthEnabled", true),
            parallaxIntensity = prefs.getFloat("parallaxIntensity", 0.5f),

            clockStyle = prefs.getString("clockStyle", "DIGITAL_NEON") ?: "DIGITAL_NEON",
            clockSize = prefs.getFloat("clockSize", 46f),
            clockFont = prefs.getString("clockFont", "MODERN_SANS") ?: "MODERN_SANS",
            clockColorHex = prefs.getString("clockColorHex", "#00F0FF") ?: "#00F0FF",
            notificationStyle = prefs.getString("notificationStyle", "IOS_GLASS") ?: "IOS_GLASS",
            lockScreenEnabled = prefs.getBoolean("lockScreenEnabled", true),
            lockPasscode = prefs.getString("lockPasscode", "1234") ?: "1234",
            lockOnLaunch = prefs.getBoolean("lockOnLaunch", false),
            lockDepthEffect = prefs.getBoolean("lockDepthEffect", true),
            lockShowNotifications = prefs.getBoolean("lockShowNotifications", true),

            badgeEnabled = prefs.getBoolean("badgeEnabled", true),
            badgeStyle = prefs.getString("badgeStyle", "NUMBER") ?: "NUMBER",

            animationsEnabled = prefs.getBoolean("animationsEnabled", true),
            animationPreset = prefs.getString("animationPreset", "SMOOTH") ?: "SMOOTH",

            gestureSwipeUp = prefs.getString("gestureSwipeUp", "APP_DRAWER") ?: "APP_DRAWER",
            gestureSwipeDown = prefs.getString("gestureSwipeDown", "CONTROL_CENTER") ?: "CONTROL_CENTER",
            gestureDoubleTap = prefs.getString("gestureDoubleTap", "LOCK_PREVIEW") ?: "LOCK_PREVIEW",
            gestureTwoFingerSwipe = prefs.getString("gestureTwoFingerSwipe", "NOTIFICATION_CENTER") ?: "NOTIFICATION_CENTER",

            lowPerformanceMode = prefs.getBoolean("lowPerformanceMode", false),
            firstRunCompleted = prefs.getBoolean("firstRunCompleted", true)
        )
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        val newSettings = transform(_settings.value)
        _settings.value = newSettings

        prefs.edit().apply {
            putString("uiMode", newSettings.uiMode)
            putString("themeMode", newSettings.themeMode)
            putFloat("glassTransparency", newSettings.glassTransparency)
            putFloat("glassBlurIntensity", newSettings.glassBlurIntensity)
            putFloat("glassBrightness", newSettings.glassBrightness)
            putFloat("glassReflection", newSettings.glassReflection)
            putFloat("glassGlowIntensity", newSettings.glassGlowIntensity)
            putFloat("glassCornerRadius", newSettings.glassCornerRadius)
            putFloat("clayShadowDepth", newSettings.clayShadowDepth)

            putInt("rows", newSettings.rows)
            putInt("columns", newSettings.columns)
            putFloat("iconSize", newSettings.iconSize)
            putString("iconShape", newSettings.iconShape)
            putString("iconPackStyle", newSettings.iconPackStyle)
            putBoolean("showLabels", newSettings.showLabels)
            putFloat("labelSize", newSettings.labelSize)
            putInt("dockItemCount", newSettings.dockItemCount)
            putFloat("dockTransparency", newSettings.dockTransparency)
            putBoolean("lockHomeScreen", newSettings.lockHomeScreen)

            putString("wallpaperType", newSettings.wallpaperType)
            putString("wallpaperUri", newSettings.wallpaperUri)
            putFloat("wallpaperBlur", newSettings.wallpaperBlur)
            putFloat("wallpaperBrightness", newSettings.wallpaperBrightness)
            putFloat("wallpaperDarkOverlay", newSettings.wallpaperDarkOverlay)
            putBoolean("wallpaperDepthEnabled", newSettings.wallpaperDepthEnabled)
            putFloat("parallaxIntensity", newSettings.parallaxIntensity)

            putString("clockStyle", newSettings.clockStyle)
            putFloat("clockSize", newSettings.clockSize)
            putString("clockFont", newSettings.clockFont)
            putString("clockColorHex", newSettings.clockColorHex)
            putString("notificationStyle", newSettings.notificationStyle)
            putBoolean("lockScreenEnabled", newSettings.lockScreenEnabled)
            putString("lockPasscode", newSettings.lockPasscode)
            putBoolean("lockOnLaunch", newSettings.lockOnLaunch)
            putBoolean("lockDepthEffect", newSettings.lockDepthEffect)
            putBoolean("lockShowNotifications", newSettings.lockShowNotifications)

            putBoolean("badgeEnabled", newSettings.badgeEnabled)
            putString("badgeStyle", newSettings.badgeStyle)

            putBoolean("animationsEnabled", newSettings.animationsEnabled)
            putString("animationPreset", newSettings.animationPreset)

            putString("gestureSwipeUp", newSettings.gestureSwipeUp)
            putString("gestureSwipeDown", newSettings.gestureSwipeDown)
            putString("gestureDoubleTap", newSettings.gestureDoubleTap)
            putString("gestureTwoFingerSwipe", newSettings.gestureTwoFingerSwipe)

            putBoolean("lowPerformanceMode", newSettings.lowPerformanceMode)
            putBoolean("firstRunCompleted", newSettings.firstRunCompleted)
            apply()
        }
    }
}
