package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.FolderItem
import com.example.data.model.HomeItem
import com.example.data.model.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class HomeLayoutRepository(
    private val context: Context,
    private val appRepository: AppRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ios27_homelayout_prefs", Context.MODE_PRIVATE)

    private val _pages = MutableStateFlow<List<List<HomeItem>>>(emptyList())
    val pages: StateFlow<List<List<HomeItem>>> = _pages.asStateFlow()

    private val _dockItems = MutableStateFlow<List<String>>(emptyList())
    val dockItems: StateFlow<List<String>> = _dockItems.asStateFlow()

    private val _folders = MutableStateFlow<Map<String, FolderItem>>(emptyMap())
    val folders: StateFlow<Map<String, FolderItem>> = _folders.asStateFlow()

    init {
        loadLayout()
        // Listen to installed apps when loaded from PackageManager to ensure apps are populated on home screen and dock
        CoroutineScope(Dispatchers.IO).launch {
            appRepository.installedApps.collect { installed ->
                if (installed.isNotEmpty()) {
                    ensureAppsPopulated(installed.map { it.packageName })
                }
            }
        }
    }

    private fun loadLayout() {
        val rawFolders = prefs.getString("folders_json", null)
        val foldersMap = mutableMapOf<String, FolderItem>()
        if (rawFolders != null) {
            try {
                val jsonArr = JSONArray(rawFolders)
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val id = obj.getString("id")
                    val name = obj.getString("name")
                    val pkgsArr = obj.getJSONArray("packages")
                    val pkgs = (0 until pkgsArr.length()).map { pkgsArr.getString(it) }
                    foldersMap[id] = FolderItem(id, name, pkgs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _folders.value = foldersMap

        val rawDock = prefs.getString("dock_json", null)
        val dockList = mutableListOf<String>()
        if (rawDock != null) {
            try {
                val jsonArr = JSONArray(rawDock)
                for (i in 0 until jsonArr.length()) {
                    dockList.add(jsonArr.getString(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val rawPages = prefs.getString("pages_json", null)
        val loadedPages = mutableListOf<List<HomeItem>>()
        if (rawPages != null) {
            try {
                val pagesArr = JSONArray(rawPages)
                for (p in 0 until pagesArr.length()) {
                    val pageArr = pagesArr.getJSONArray(p)
                    val items = mutableListOf<HomeItem>()
                    for (i in 0 until pageArr.length()) {
                        val itemObj = pageArr.getJSONObject(i)
                        when (itemObj.getString("type")) {
                            "APP" -> items.add(HomeItem.App(itemObj.getString("pkg")))
                            "FOLDER" -> items.add(HomeItem.Folder(itemObj.getString("folderId")))
                            "WIDGET" -> {
                                val wType = WidgetType.valueOf(itemObj.getString("widgetType"))
                                items.add(HomeItem.Widget(wType))
                            }
                            "SYSTEM_WIDGET" -> {
                                val widgetId = itemObj.getInt("appWidgetId")
                                items.add(HomeItem.SystemWidget(widgetId))
                            }
                        }
                    }
                    loadedPages.add(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (loadedPages.isNotEmpty()) {
            _pages.value = loadedPages
            _dockItems.value = dockList
        } else {
            _pages.value = listOf(
                listOf(HomeItem.Widget(WidgetType.CLOCK), HomeItem.Widget(WidgetType.BATTERY))
            )
        }
    }

    private fun ensureAppsPopulated(installedPkgs: List<String>) {
        val currentPages = _pages.value
        val hasAnyApps = currentPages.any { page -> page.any { it is HomeItem.App || it is HomeItem.Folder } }

        if (!hasAnyApps || currentPages.isEmpty()) {
            // Populate default pages with widgets and apps
            val page1 = mutableListOf<HomeItem>(
                HomeItem.Widget(WidgetType.CLOCK),
                HomeItem.Widget(WidgetType.BATTERY)
            )
            installedPkgs.take(8).forEach { pkg ->
                page1.add(HomeItem.App(pkg))
            }

            val page2 = mutableListOf<HomeItem>(
                HomeItem.Widget(WidgetType.WEATHER),
                HomeItem.Widget(WidgetType.CALENDAR)
            )
            installedPkgs.drop(8).take(12).forEach { pkg ->
                page2.add(HomeItem.App(pkg))
            }

            // Create a third page if there are more apps
            val page3 = mutableListOf<HomeItem>()
            installedPkgs.drop(20).take(16).forEach { pkg ->
                page3.add(HomeItem.App(pkg))
            }

            val newPages = if (page3.isNotEmpty()) listOf(page1, page2, page3) else listOf(page1, page2)
            _pages.value = newPages

            if (_dockItems.value.isEmpty()) {
                val dock = mutableListOf<String>()
                val phone = installedPkgs.firstOrNull { it.contains("dialer") || it.contains("phone") || it.contains("call") }
                if (phone != null) dock.add(phone)
                val msg = installedPkgs.firstOrNull { it.contains("messaging") || it.contains("message") || it.contains("sms") }
                if (msg != null && !dock.contains(msg)) dock.add(msg)
                val browser = installedPkgs.firstOrNull { it.contains("chrome") || it.contains("browser") }
                if (browser != null && !dock.contains(browser)) dock.add(browser)
                val camera = installedPkgs.firstOrNull { it.contains("camera") }
                if (camera != null && !dock.contains(camera)) dock.add(camera)

                installedPkgs.forEach { pkg ->
                    if (dock.size < 4 && !dock.contains(pkg)) {
                        dock.add(pkg)
                    }
                }
                _dockItems.value = dock
            }
            saveLayout()
        } else if (_dockItems.value.isEmpty()) {
            _dockItems.value = installedPkgs.take(4)
            saveLayout()
        }
    }

    fun saveLayout() {
        val pagesArr = JSONArray()
        _pages.value.forEach { page ->
            val pageArr = JSONArray()
            page.forEach { item ->
                val obj = JSONObject()
                when (item) {
                    is HomeItem.App -> {
                        obj.put("type", "APP")
                        obj.put("pkg", item.packageName)
                    }
                    is HomeItem.Folder -> {
                        obj.put("type", "FOLDER")
                        obj.put("folderId", item.folderId)
                    }
                    is HomeItem.Widget -> {
                        obj.put("type", "WIDGET")
                        obj.put("widgetType", item.widgetType.name)
                    }
                    is HomeItem.SystemWidget -> {
                        obj.put("type", "SYSTEM_WIDGET")
                        obj.put("appWidgetId", item.appWidgetId)
                    }
                }
                pageArr.put(obj)
            }
            pagesArr.put(pageArr)
        }

        val dockArr = JSONArray()
        _dockItems.value.forEach { dockArr.put(it) }

        val foldersArr = JSONArray()
        _folders.value.values.forEach { folder ->
            val obj = JSONObject()
            obj.put("id", folder.id)
            obj.put("name", folder.name)
            val pkgsArr = JSONArray()
            folder.packageNames.forEach { pkgsArr.put(it) }
            obj.put("packages", pkgsArr)
            foldersArr.put(obj)
        }

        prefs.edit().apply {
            putString("pages_json", pagesArr.toString())
            putString("dock_json", dockArr.toString())
            putString("folders_json", foldersArr.toString())
            apply()
        }
    }

    fun addPage() {
        val current = _pages.value.toMutableList()
        current.add(emptyList())
        _pages.value = current
        saveLayout()
    }

    fun removePage(pageIndex: Int) {
        val current = _pages.value.toMutableList()
        if (current.size > 1 && pageIndex in current.indices) {
            val removedItems = current.removeAt(pageIndex)
            if (removedItems.isNotEmpty()) {
                val targetIndex = (pageIndex - 1).coerceAtLeast(0)
                current[targetIndex] = current[targetIndex] + removedItems
            }
            _pages.value = current
            saveLayout()
        }
    }

    fun addAppToPage(pageIndex: Int, packageName: String) {
        val current = _pages.value.toMutableList()
        val safeIndex = pageIndex.coerceIn(0, (current.size - 1).coerceAtLeast(0))
        if (current.isEmpty()) {
            current.add(listOf(HomeItem.App(packageName)))
        } else {
            val pageList = current[safeIndex].toMutableList()
            pageList.add(HomeItem.App(packageName))
            current[safeIndex] = pageList
        }
        _pages.value = current
        saveLayout()
    }

    fun addWidgetToPage(pageIndex: Int, widgetType: WidgetType) {
        val current = _pages.value.toMutableList()
        val safeIndex = pageIndex.coerceIn(0, (current.size - 1).coerceAtLeast(0))
        if (current.isEmpty()) {
            current.add(listOf(HomeItem.Widget(widgetType)))
        } else {
            val pageList = current[safeIndex].toMutableList()
            pageList.add(0, HomeItem.Widget(widgetType))
            current[safeIndex] = pageList
        }
        _pages.value = current
        saveLayout()
    }

    fun addSystemWidgetToPage(pageIndex: Int, appWidgetId: Int) {
        val current = _pages.value.toMutableList()
        val safeIndex = pageIndex.coerceIn(0, (current.size - 1).coerceAtLeast(0))
        if (current.isEmpty()) {
            current.add(listOf(HomeItem.SystemWidget(appWidgetId)))
        } else {
            val pageList = current[safeIndex].toMutableList()
            pageList.add(0, HomeItem.SystemWidget(appWidgetId))
            current[safeIndex] = pageList
        }
        _pages.value = current
        saveLayout()
    }

    fun removeItem(pageIndex: Int, itemIndex: Int) {
        val current = _pages.value.toMutableList()
        if (pageIndex in current.indices) {
            val pageList = current[pageIndex].toMutableList()
            if (itemIndex in pageList.indices) {
                pageList.removeAt(itemIndex)
                current[pageIndex] = pageList
                _pages.value = current
                saveLayout()
            }
        }
    }

    fun moveItem(pageIndex: Int, fromIndex: Int, toIndex: Int) {
        val current = _pages.value.toMutableList()
        if (pageIndex in current.indices) {
            val pageList = current[pageIndex].toMutableList()
            if (fromIndex in pageList.indices && toIndex in pageList.indices && fromIndex != toIndex) {
                val item = pageList.removeAt(fromIndex)
                pageList.add(toIndex, item)
                current[pageIndex] = pageList
                _pages.value = current
                saveLayout()
            }
        }
    }

    fun moveItemToPage(fromPage: Int, itemIndex: Int, toPage: Int) {
        val current = _pages.value.toMutableList()
        if (fromPage in current.indices && toPage in current.indices && fromPage != toPage) {
            val fromList = current[fromPage].toMutableList()
            if (itemIndex in fromList.indices) {
                val item = fromList.removeAt(itemIndex)
                current[fromPage] = fromList
                val toList = current[toPage].toMutableList()
                toList.add(item)
                current[toPage] = toList
                _pages.value = current
                saveLayout()
            }
        }
    }

    fun createFolder(name: String, pageIndex: Int, initialPackages: List<String>): String {
        val folderId = "folder_${System.currentTimeMillis()}"
        val folder = FolderItem(folderId, name, initialPackages)
        val newFolders = _folders.value.toMutableMap()
        newFolders[folderId] = folder
        _folders.value = newFolders

        val currentPages = _pages.value.toMutableList()
        val safeIndex = pageIndex.coerceIn(0, (currentPages.size - 1).coerceAtLeast(0))
        if (currentPages.isNotEmpty()) {
            val pageList = currentPages[safeIndex].toMutableList()
            pageList.add(HomeItem.Folder(folderId))
            currentPages[safeIndex] = pageList
            _pages.value = currentPages
        } else {
            currentPages.add(listOf(HomeItem.Folder(folderId)))
            _pages.value = currentPages
        }
        saveLayout()
        return folderId
    }

    fun createFolderWithApps(name: String, pageIndex: Int, pkg1: String, pkg2: String): String {
        val currentPages = _pages.value.toMutableList()
        if (pageIndex in currentPages.indices) {
            val pageList = currentPages[pageIndex].toMutableList()
            pageList.removeAll { it is HomeItem.App && (it.packageName == pkg1 || it.packageName == pkg2) }
            currentPages[pageIndex] = pageList
            _pages.value = currentPages
        }
        return createFolder(name, pageIndex, listOf(pkg1, pkg2))
    }

    fun updateFolderName(folderId: String, newName: String) {
        val current = _folders.value.toMutableMap()
        val existing = current[folderId] ?: return
        current[folderId] = existing.copy(name = newName)
        _folders.value = current
        saveLayout()
    }

    fun addAppToFolder(folderId: String, packageName: String) {
        val current = _folders.value.toMutableMap()
        val existing = current[folderId] ?: return
        if (!existing.packageNames.contains(packageName)) {
            current[folderId] = existing.copy(packageNames = existing.packageNames + packageName)
            _folders.value = current
            saveLayout()
        }
    }

    fun removeAppFromFolder(folderId: String, packageName: String) {
        val current = _folders.value.toMutableMap()
        val existing = current[folderId] ?: return
        current[folderId] = existing.copy(packageNames = existing.packageNames.filter { it != packageName })
        _folders.value = current
        saveLayout()
    }

    fun deleteFolder(folderId: String) {
        val current = _folders.value.toMutableMap()
        current.remove(folderId)
        _folders.value = current

        val currentPages = _pages.value.map { page ->
            page.filterNot { it is HomeItem.Folder && it.folderId == folderId }
        }
        _pages.value = currentPages
        saveLayout()
    }

    fun updateDockItems(newDock: List<String>) {
        _dockItems.value = newDock
        saveLayout()
    }

    fun addAppToDock(packageName: String) {
        val current = _dockItems.value.toMutableList()
        if (!current.contains(packageName) && current.size < 6) {
            current.add(packageName)
            _dockItems.value = current
            saveLayout()
        }
    }

    fun removeAppFromDock(packageName: String) {
        val current = _dockItems.value.toMutableList()
        current.remove(packageName)
        _dockItems.value = current
        saveLayout()
    }
}
