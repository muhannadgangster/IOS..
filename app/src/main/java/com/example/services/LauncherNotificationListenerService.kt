package com.example.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.model.NotificationItem
import com.example.data.repository.NotificationRepository

class LauncherNotificationListenerService : NotificationListenerService() {

    companion object {
        var activeInstance: LauncherNotificationListenerService? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeInstance = this
        syncActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        if (sbn.isOngoing) return // Skip ongoing / media players in regular notification feed

        val item = parseNotification(sbn) ?: return
        NotificationRepository.addNotification(item)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn ?: return
        NotificationRepository.removeNotification(sbn.key)
    }

    private fun syncActiveNotifications() {
        try {
            val sbns = activeNotifications ?: return
            val items = sbns.filterNot { it.isOngoing }.mapNotNull { parseNotification(it) }
            NotificationRepository.updateNotifications(items)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseNotification(sbn: StatusBarNotification): NotificationItem? {
        val extras = sbn.notification.extras ?: return null
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        if (title.isBlank() && text.isBlank()) return null

        val pkg = sbn.packageName
        val appName = try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            pkg
        }

        return NotificationItem(
            key = sbn.key,
            packageName = pkg,
            appName = appName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )
    }
}
