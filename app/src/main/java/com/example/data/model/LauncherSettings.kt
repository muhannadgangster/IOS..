package com.example.data.model

data class LauncherSettings(
    // Appearance & Style
    val uiMode: String = "GLASS", // "GLASS", "CLAY", "MIXED"
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val glassTransparency: Float = 0.35f,
    val glassBlurIntensity: Float = 16f,
    val glassBrightness: Float = 1.0f,
    val glassReflection: Float = 0.7f,
    val glassGlowIntensity: Float = 0.6f,
    val glassCornerRadius: Float = 22f,
    val clayShadowDepth: Float = 8f,

    // Home Grid & Layout
    val rows: Int = 5,
    val columns: Int = 4,
    val iconSize: Float = 58f,
    val iconShape: String = "SQUIRCLE", // "SQUIRCLE", "ROUNDED_SQUARE", "CIRCLE", "SQUARE"
    val iconPackStyle: String = "IOS_GLASS", // "IOS_GLASS", "CLAY", "NEON", "MINIMAL", "DEFAULT"
    val showLabels: Boolean = true,
    val labelSize: Float = 12f,
    val dockItemCount: Int = 4,
    val dockTransparency: Float = 0.35f,
    val lockHomeScreen: Boolean = false,

    // Wallpaper
    val wallpaperType: String = "BUILTIN_GLASS", // "BUILTIN_GLASS", "BUILTIN_CLAY", "GRADIENT_AURA", "GRADIENT_SUNSET", "SOLID_OBSIDIAN", "GALLERY"
    val wallpaperUri: String? = null,
    val wallpaperBlur: Float = 0f,
    val wallpaperBrightness: Float = 1.0f,
    val wallpaperDarkOverlay: Float = 0.15f,
    val wallpaperDepthEnabled: Boolean = true,
    val parallaxIntensity: Float = 0.5f,

    // Clock & Lock Screen
    val clockStyle: String = "DIGITAL_NEON", // "DIGITAL_NEON", "LIQUID_MINIMAL", "BOLD_NUMBERS", "ANALOG_GLASS"
    val clockSize: Float = 46f,
    val clockFont: String = "MODERN_SANS",
    val clockColorHex: String = "#00F0FF",
    val notificationStyle: String = "IOS_GLASS", // "IOS_GLASS", "COMPACT", "MINIMAL"
    val lockScreenEnabled: Boolean = true,
    val lockPasscode: String = "1234",
    val lockOnLaunch: Boolean = false,
    val lockDepthEffect: Boolean = true,
    val lockShowNotifications: Boolean = true,

    // Badges & Notifications
    val badgeEnabled: Boolean = true,
    val badgeStyle: String = "NUMBER", // "NUMBER", "DOT"

    // Animations
    val animationsEnabled: Boolean = true,
    val animationPreset: String = "SMOOTH", // "FAST", "NORMAL", "SMOOTH", "CINEMATIC"

    // Gestures
    val gestureSwipeUp: String = "APP_DRAWER",
    val gestureSwipeDown: String = "CONTROL_CENTER",
    val gestureDoubleTap: String = "LOCK_PREVIEW",
    val gestureTwoFingerSwipe: String = "NOTIFICATION_CENTER",

    // Performance & State
    val lowPerformanceMode: Boolean = false,
    val firstRunCompleted: Boolean = true
)
