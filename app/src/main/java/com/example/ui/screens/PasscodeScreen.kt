package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PasscodeScreen(
    settings: LauncherSettings,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val coroutineScope = rememberCoroutineScope()

    var enteredPasscode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Shake animation on error
    val shakeOffset = remember { Animatable(0f) }

    // Drag-down to cancel/slide back to Lock Screen
    val dragOffsetAnim = remember { Animatable(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    val targetPasscode = remember(settings.lockPasscode) {
        if (settings.lockPasscode.isNotBlank()) settings.lockPasscode else "1234"
    }

    // Keypad press handler
    fun onDigitPress(digit: String) {
        if (enteredPasscode.length < targetPasscode.length && !isSuccess) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            val next = enteredPasscode + digit
            enteredPasscode = next

            if (next.length == targetPasscode.length) {
                if (next == targetPasscode) {
                    isSuccess = true
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    coroutineScope.launch {
                        delay(220)
                        onUnlocked()
                    }
                } else {
                    isError = true
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 50, 40, 50),
                            intArrayOf(0, 200, 0, 200),
                            -1
                        )
                    )
                    coroutineScope.launch {
                        // Shake effect
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -25f at 50
                                25f at 100
                                -20f at 150
                                20f at 200
                                -10f at 250
                                10f at 300
                                0f at 400
                            }
                        )
                        delay(100)
                        enteredPasscode = ""
                        isError = false
                    }
                }
            }
        }
    }

    fun onDeletePress() {
        if (enteredPasscode.isNotEmpty()) {
            vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            enteredPasscode = enteredPasscode.dropLast(1)
        }
    }

    fun onFaceIdQuickUnlock() {
        isSuccess = true
        vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
        coroutineScope.launch {
            delay(280)
            onUnlocked()
        }
    }

    fun openEmergencyDialer() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // Infinite pulsing for Face ID scanner
    val infiniteTransition = rememberInfiniteTransition(label = "face_id_pulse")
    val scanScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, dragOffsetAnim.value.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y
                        if (totalDragY > 0) {
                            coroutineScope.launch {
                                dragOffsetAnim.snapTo(totalDragY.coerceAtMost(600f))
                            }
                        }
                    },
                    onDragEnd = {
                        if (totalDragY > 120f) {
                            onCancel()
                        } else {
                            coroutineScope.launch {
                                dragOffsetAnim.animateTo(0f, tween(250))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffsetAnim.animateTo(0f, tween(250))
                        }
                    }
                )
            }
            // Liquid Frosted Blur Overlay
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE6050914),
                        Color(0xF2080E21),
                        Color(0xFA04060E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section: Face ID icon & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                // Padlock / Face ID Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(if (isSuccess) 1.15f else scanScale)
                        .clip(CircleShape)
                        .background(
                            if (isSuccess) Color(0x3300E676)
                            else if (isError) Color(0x33FF1744)
                            else Color(0x3300F0FF)
                        )
                        .border(
                            1.5.dp,
                            if (isSuccess) Color(0xFF00E676)
                            else if (isError) Color(0xFFFF1744)
                            else NeonCyan,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle
                        else if (enteredPasscode.isNotEmpty()) Icons.Default.LockOpen
                        else Icons.Default.Lock,
                        contentDescription = "Security State",
                        tint = if (isSuccess) Color(0xFF00E676)
                        else if (isError) Color(0xFFFF1744)
                        else NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isSuccess) "Unlocked" else "Enter Passcode",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isError) "Incorrect Passcode. Try again." else "Touch Face ID or Enter PIN (Default: $targetPasscode)",
                    color = if (isError) Color(0xFFFF5252) else Color(0x99BAC7E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Passcode Dots
                Row(
                    modifier = Modifier
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(targetPasscode.length) { index ->
                        val isFilled = index < enteredPasscode.length
                        val dotScale by animateFloatAsState(
                            targetValue = if (isFilled) 1.25f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                            label = "dot_scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .scale(dotScale)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSuccess -> Color(0xFF00E676)
                                        isError -> Color(0xFFFF1744)
                                        isFilled -> Color.White
                                        else -> Color(0x22FFFFFF)
                                    }
                                )
                                .border(
                                    1.2.dp,
                                    when {
                                        isSuccess -> Color(0xFF00E676)
                                        isError -> Color(0xFFFF1744)
                                        isFilled -> Color.White
                                        else -> Color(0x66FFFFFF)
                                    },
                                    CircleShape
                                )
                        )
                    }
                }
            }

            // Keypad Grid Section (3 columns x 4 rows)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keyRows = listOf(
                    listOf("1" to "", "2" to "A B C", "3" to "D E F"),
                    listOf("4" to "G H I", "5" to "J K L", "6" to "M N O"),
                    listOf("7" to "P Q R S", "8" to "T U V", "9" to "W X Y Z")
                )

                keyRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { (number, letters) ->
                            KeypadButton(
                                number = number,
                                letters = letters,
                                onClick = { onDigitPress(number) }
                            )
                        }
                    }
                }

                // Row 4: Emergency, 0, Cancel/Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Emergency or Face ID
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable { openEmergencyDialer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Emergency",
                            color = Color(0xB3BAC7E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Center: 0
                    KeypadButton(
                        number = "0",
                        letters = "+",
                        onClick = { onDigitPress("0") }
                    )

                    // Right: Backspace or Cancel
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (enteredPasscode.isNotEmpty()) {
                                    onDeletePress()
                                } else {
                                    onCancel()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (enteredPasscode.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Cancel",
                                color = Color(0xB3BAC7E5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom Section: Face ID Quick Bypass Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x33101E3E))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .clickable { onFaceIdQuickUnlock() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Face ID",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap Face ID to Unlock",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Home Bar Pill
                Box(
                    modifier = Modifier
                        .width(135.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onCancel() }
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    number: String,
    letters: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.5f),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) Color(0x5500F0FF)
                else Color(0x38131C38)
            )
            .border(
                1.dp,
                if (isPressed) NeonCyan else Color(0x33FFFFFF),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 30.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = Color(0xB3BAC7E5),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
