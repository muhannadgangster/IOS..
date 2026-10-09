package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.LauncherApplication

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val app = context.applicationContext as? LauncherApplication
        app?.appRepository?.loadAppsAsync()
    }
}
