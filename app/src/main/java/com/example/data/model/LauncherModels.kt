package com.example.data.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val installTime: Long = 0L,
    val category: String = "Utilities",
    @Transient val iconDrawable: Drawable? = null
)

data class FolderItem(
    val id: String,
    val name: String,
    val packageNames: List<String> = emptyList(),
    val pageIndex: Int = 0,
    val gridPosition: Int = 0
)

enum class WidgetType {
    CLOCK,
    WEATHER,
    BATTERY,
    CALENDAR,
    MUSIC,
    DEVICE_INFO
}

sealed interface HomeItem {
    data class App(val packageName: String, val customLabel: String? = null) : HomeItem
    data class Folder(val folderId: String) : HomeItem
    data class Widget(val widgetType: WidgetType, val spanX: Int = 2, val spanY: Int = 2) : HomeItem
    data class SystemWidget(val appWidgetId: Int, val spanX: Int = 2, val spanY: Int = 2) : HomeItem
}

data class NotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val isRead: Boolean = false
)

data class ControlTile(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val iconName: String,
    val isEnabled: Boolean = false,
    val isSystemToggle: Boolean = true
)
