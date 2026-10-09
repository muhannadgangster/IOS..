package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.data.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AppRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ios27_app_prefs", Context.MODE_PRIVATE)

    private val iconCache = mutableMapOf<String, Drawable>()

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _favoritePackages = MutableStateFlow<Set<String>>(loadFavorites())
    val favoritePackages: StateFlow<Set<String>> = _favoritePackages.asStateFlow()

    private val _hiddenPackages = MutableStateFlow<Set<String>>(loadHidden())
    val hiddenPackages: StateFlow<Set<String>> = _hiddenPackages.asStateFlow()

    init {
        loadAppsAsync()
    }

    private fun loadFavorites(): Set<String> {
        return prefs.getStringSet("favorite_packages", emptySet()) ?: emptySet()
    }

    private fun loadHidden(): Set<String> {
        return prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()
    }

    fun loadAppsAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedActivities = pm.queryIntentActivities(mainIntent, 0)
            val currentPkg = context.packageName

            val favorites = _favoritePackages.value
            val hidden = _hiddenPackages.value

            val appList = resolvedActivities.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                // Do not display this launcher inside its own app drawer
                if (pkgName == currentPkg) return@mapNotNull null

                val activityName = resolveInfo.activityInfo.name
                val label = resolveInfo.loadLabel(pm).toString()

                val icon = iconCache.getOrPut(pkgName) {
                    try {
                        resolveInfo.loadIcon(pm)
                    } catch (e: Exception) {
                        pm.defaultActivityIcon
                    }
                }

                val installTime = try {
                    pm.getPackageInfo(pkgName, 0).firstInstallTime
                } catch (e: Exception) {
                    0L
                }

                val category = guessCategory(resolveInfo.activityInfo.applicationInfo, label, pkgName)

                AppItem(
                    packageName = pkgName,
                    activityName = activityName,
                    label = label,
                    isFavorite = favorites.contains(pkgName),
                    isHidden = hidden.contains(pkgName),
                    installTime = installTime,
                    category = category,
                    iconDrawable = icon
                )
            }.sortedBy { it.label.lowercase(Locale.getDefault()) }

            _installedApps.value = appList
        }
    }

    private fun guessCategory(appInfo: ApplicationInfo, label: String, pkg: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return "Games"
                ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO -> return "Media"
                ApplicationInfo.CATEGORY_SOCIAL -> return "Social"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "Work"
                ApplicationInfo.CATEGORY_IMAGE -> return "Media"
                ApplicationInfo.CATEGORY_MAPS -> return "Tools"
            }
        }
        val lowerPkg = pkg.lowercase(Locale.ROOT)
        val lowerLabel = label.lowercase(Locale.ROOT)
        return when {
            lowerPkg.contains("game") || lowerLabel.contains("game") -> "Games"
            lowerPkg.contains("social") || lowerPkg.contains("chat") || lowerPkg.contains("message") || lowerPkg.contains("whatsapp") || lowerPkg.contains("twitter") || lowerPkg.contains("facebook") || lowerPkg.contains("instagram") -> "Social"
            lowerPkg.contains("media") || lowerPkg.contains("music") || lowerPkg.contains("video") || lowerPkg.contains("youtube") || lowerPkg.contains("spotify") || lowerPkg.contains("player") || lowerPkg.contains("photo") || lowerPkg.contains("gallery") -> "Media"
            lowerPkg.contains("doc") || lowerPkg.contains("office") || lowerPkg.contains("work") || lowerPkg.contains("sheet") || lowerPkg.contains("slide") || lowerPkg.contains("mail") || lowerPkg.contains("email") -> "Work"
            else -> "Tools"
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return iconCache[packageName] ?: try {
            val pm = context.packageManager
            val icon = pm.getApplicationIcon(packageName)
            iconCache[packageName] = icon
            icon
        } catch (e: Exception) {
            null
        }
    }

    fun toggleFavorite(packageName: String) {
        val current = _favoritePackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.edit().putStringSet("favorite_packages", current).apply()
        _favoritePackages.value = current
        loadAppsAsync()
    }

    fun toggleHidden(packageName: String) {
        val current = _hiddenPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.edit().putStringSet("hidden_packages", current).apply()
        _hiddenPackages.value = current
        loadAppsAsync()
    }

    fun launchApp(packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
