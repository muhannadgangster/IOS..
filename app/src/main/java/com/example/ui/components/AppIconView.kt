package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet

fun drawableToBitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconView(
    app: AppItem,
    settings: LauncherSettings,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val iconBitmap = remember(app.packageName, app.iconDrawable) {
        drawableToBitmap(app.iconDrawable)?.asImageBitmap()
    }

    val iconSizeDp: Dp = settings.iconSize.dp
    val iconShape: Shape = remember(settings.iconShape) {
        when (settings.iconShape) {
            "CIRCLE" -> CircleShape
            "ROUNDED_SQUARE" -> RoundedCornerShape(14.dp)
            "SQUARE" -> RoundedCornerShape(4.dp)
            else -> RoundedCornerShape(18.dp) // SQUIRCLE
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("app_icon_${app.packageName}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(iconSizeDp + 6.dp)
        ) {
            // Icon Styling Container based on iconPackStyle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(iconSizeDp)
                    .shadow(
                        elevation = if (settings.lowPerformanceMode) 2.dp else 6.dp,
                        shape = iconShape,
                        spotColor = NeonCyan.copy(alpha = 0.3f)
                    )
                    .clip(iconShape)
                    .then(
                        when (settings.iconPackStyle) {
                            "IOS_GLASS" -> Modifier
                                .background(Color(0x33101830))
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.6f),
                                            NeonCyan.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    iconShape
                                )
                                .drawBehind {
                                    // top specular glass sheen
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.35f),
                                                Color.Transparent
                                            ),
                                            startY = 0f,
                                            endY = size.height * 0.45f
                                        )
                                    )
                                }
                            "NEON" -> Modifier
                                .background(Color(0xFF090D1C))
                                .border(
                                    1.5.dp,
                                    Brush.sweepGradient(
                                        listOf(NeonCyan, NeonViolet, NeonPink, NeonCyan)
                                    ),
                                    iconShape
                                )
                            "CLAY" -> Modifier
                                .background(Color(0xFF1B233C))
                                .border(1.dp, Color(0x336F8AD0), iconShape)
                            "MINIMAL" -> Modifier
                                .background(Color(0x4D000000))
                                .border(0.5.dp, Color(0x33FFFFFF), iconShape)
                            else -> Modifier
                        }
                    )
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(iconSizeDp * 0.88f)
                            .clip(iconShape)
                    )
                }
            }

            // Notification Badge
            if (settings.badgeEnabled && badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(if (settings.badgeStyle == "DOT") 10.dp else 19.dp)
                        .clip(CircleShape)
                        .background(NeonPink)
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    if (settings.badgeStyle != "DOT") {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (settings.showLabels) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                fontSize = settings.labelSize.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
