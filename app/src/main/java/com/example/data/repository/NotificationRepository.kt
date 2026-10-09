package com.example.data.repository

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.data.model.NotificationItem
import com.example.services.LauncherNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun updateNotifications(items: List<NotificationItem>) {
        _notifications.value = items
    }

    fun addNotification(item: NotificationItem) {
        val current = _notifications.value.filter { it.key != item.key }
        _notifications.value = listOf(item) + current
    }

    fun removeNotification(key: String) {
        _notifications.value = _notifications.value.filter { it.key != key }
        LauncherNotificationListenerService.activeInstance?.cancelNotification(key)
    }

    fun clearAll() {
        _notifications.value = emptyList()
        LauncherNotificationListenerService.activeInstance?.cancelAllNotifications()
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val myComponent = ComponentName(context, LauncherNotificationListenerService::class.java).flattenToString()
        return enabledListeners.contains(myComponent)
    }
}
