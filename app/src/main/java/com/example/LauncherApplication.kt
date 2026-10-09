package com.example

import android.app.Application
import com.example.data.repository.AppRepository
import com.example.data.repository.HomeLayoutRepository
import com.example.data.repository.SettingsRepository

class LauncherApplication : Application() {
    lateinit var appRepository: AppRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var homeLayoutRepository: HomeLayoutRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        appRepository = AppRepository(this)
        homeLayoutRepository = HomeLayoutRepository(this, appRepository)
    }
}
