package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppIconView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.adaptiveGlassOrClay

data class ContactItem(val name: String, val number: String = "")

@Composable
fun SearchScreen(
    allApps: List<AppItem>,
    settings: LauncherSettings,
    onBackToHome: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onNavigateSetting: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var contactsList by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestContactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
    }

    // Load contacts when permission granted
    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission) {
            try {
                val list = mutableListOf<ContactItem>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext() && list.size < 50) {
                        val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "" else ""
                        val num = if (numIdx >= 0) it.getString(numIdx) ?: "" else ""
                        if (name.isNotBlank()) list.add(ContactItem(name, num))
                    }
                }
                contactsList = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val matchedApps = remember(query, allApps) {
        if (query.isBlank()) emptyList()
        else allApps.filter {
            !it.isHidden && (it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true))
        }
    }

    val settingShortcuts = listOf(
        "Appearance" to "Customize Glass & Clay UI, Transparency, Blur",
        "Home Screen" to "Change grid rows, columns, icon size",
        "Icons" to "Icon shapes, neon border, glass styles",
        "Wallpaper" to "Set live wallpaper, dark overlay, blur",
        "Lock Screen" to "Lock screen clock styles, passcode, PIN, Face ID security",
        "Control Center" to "Configure quick tiles, sliders",
        "Default Launcher" to "Make iOS 27 by mun. default Home",
        "About" to "Version and developer information"
    )

    val matchedSettings = remember(query) {
        if (query.isBlank()) emptyList()
        else settingShortcuts.filter { it.first.contains(query, ignoreCase = true) || it.second.contains(query, ignoreCase = true) }
    }

    val matchedContacts = remember(query, contactsList) {
        if (query.isBlank()) emptyList()
        else contactsList.filter { it.name.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 14.dp)
    ) {
        // Search Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x33141B33))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps, settings, contacts...", color = Color(0x99BAC7E5), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x4010162B),
                    unfocusedContainerColor = Color(0x3310162B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("universal_search_input")
            )
        }

        if (query.isBlank()) {
            // Recent / Suggested Queries
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "QUICK SHORTCUTS",
                    color = Color(0xB3BAC7E5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Settings", "Wallpaper", "Camera", "Browser").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33141E38))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                .clickable { query = tag }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = tag, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Matched Apps
                if (matchedApps.isNotEmpty()) {
                    item {
                        Text(
                            text = "APPLICATIONS",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(matchedApps.take(6)) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .adaptiveGlassOrClay(
                                    uiMode = settings.uiMode,
                                    cornerRadius = 16.dp,
                                    transparency = 0.35f,
                                    glowColor = NeonCyan
                                )
                                .clickable { onLaunchApp(app.packageName) }
                                .padding(12.dp)
                        ) {
                            AppIconView(
                                app = app,
                                settings = settings.copy(iconSize = 44f, showLabels = false),
                                onClick = { onLaunchApp(app.packageName) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = app.category,
                                    color = Color(0x99BAC7E5),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Matched Launcher Settings
                if (matchedSettings.isNotEmpty()) {
                    item {
                        Text(
                            text = "LAUNCHER SETTINGS",
                            color = NeonViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(matchedSettings) { (title, desc) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .adaptiveGlassOrClay(
                                    uiMode = settings.uiMode,
                                    cornerRadius = 16.dp,
                                    transparency = 0.35f,
                                    glowColor = NeonViolet
                                )
                                .clickable { onNavigateSetting(title) }
                                .padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = title,
                                tint = NeonViolet,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = desc,
                                    color = Color(0x99BAC7E5),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Matched Contacts
                if (matchedContacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "CONTACTS",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(matchedContacts.take(5)) { contact ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .adaptiveGlassOrClay(
                                    uiMode = settings.uiMode,
                                    cornerRadius = 16.dp,
                                    transparency = 0.35f,
                                    glowColor = NeonPink
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPage,
                                contentDescription = "Contact",
                                tint = NeonPink,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = contact.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (contact.number.isNotEmpty()) {
                                    Text(
                                        text = contact.number,
                                        color = Color(0x99BAC7E5),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (!hasContactsPermission) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .adaptiveGlassOrClay(settings.uiMode, 16.dp, 0.3f, NeonPink)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Search Device Contacts",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Enable permission to find contacts in search",
                                        color = Color(0x99BAC7E5),
                                        fontSize = 10.sp
                                    )
                                }
                                ElevatedButton(
                                    onClick = { requestContactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                    colors = ButtonDefaults.elevatedButtonColors(containerColor = NeonPink)
                                ) {
                                    Text("Allow", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                if (matchedApps.isEmpty() && matchedSettings.isEmpty() && matchedContacts.isEmpty()) {
                    item {
                        Text(
                            text = "No results for \"$query\"",
                            color = Color(0x99BAC7E5),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }
            }
        }
    }
}
