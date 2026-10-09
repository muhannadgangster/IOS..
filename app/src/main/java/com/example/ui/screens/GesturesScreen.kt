package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.adaptiveGlassOrClay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturesScreen(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        "APP_DRAWER" to "App Drawer",
        "CONTROL_CENTER" to "Control Center",
        "NOTIFICATION_CENTER" to "Notification Center",
        "LOCK_PREVIEW" to "Lock Screen Preview",
        "SEARCH" to "Universal Search"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
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
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Launcher Gestures",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure custom swipe & tap triggers",
                    color = Color(0x99BAC7E5),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveGlassOrClay(settings.uiMode, 22.dp, 0.35f, NeonCyan)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Swipe Up
                GestureDropdownItem(
                    label = "Swipe Up",
                    desc = "Default opens App Drawer",
                    currentValue = settings.gestureSwipeUp,
                    options = actions,
                    onSelect = { onUpdateSettings { s -> s.copy(gestureSwipeUp = it) } }
                )

                // Swipe Down
                GestureDropdownItem(
                    label = "Swipe Down",
                    desc = "Default opens Control Center",
                    currentValue = settings.gestureSwipeDown,
                    options = actions,
                    onSelect = { onUpdateSettings { s -> s.copy(gestureSwipeDown = it) } }
                )

                // Double Tap
                GestureDropdownItem(
                    label = "Double Tap on Empty Space",
                    desc = "Default opens Lock Screen Preview",
                    currentValue = settings.gestureDoubleTap,
                    options = actions,
                    onSelect = { onUpdateSettings { s -> s.copy(gestureDoubleTap = it) } }
                )

                // Two Finger Swipe
                GestureDropdownItem(
                    label = "Two-Finger Swipe",
                    desc = "Default opens Notification Center",
                    currentValue = settings.gestureTwoFingerSwipe,
                    options = actions,
                    onSelect = { onUpdateSettings { s -> s.copy(gestureTwoFingerSwipe = it) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureDropdownItem(
    label: String,
    desc: String,
    currentValue: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == currentValue }?.second ?: currentValue

    Column {
        Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(text = desc, color = Color(0x99BAC7E5), fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x22101830),
                    unfocusedContainerColor = Color(0x22101830),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF0F1528))
            ) {
                options.forEach { (key, labelText) ->
                    DropdownMenuItem(
                        text = { Text(labelText, color = Color.White) },
                        onClick = {
                            onSelect(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
