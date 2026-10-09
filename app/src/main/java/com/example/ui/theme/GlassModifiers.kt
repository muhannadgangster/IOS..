package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a Liquid Glass frosted style with customizable transparency,
 * subtle cyan/violet specular borders, and top specular reflections.
 */
fun Modifier.liquidGlass(
    cornerRadius: Dp = 22.dp,
    transparency: Float = 0.35f,
    borderAlpha: Float = 0.35f,
    reflectionAlpha: Float = 0.4f,
    glowColor: Color = NeonCyan
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val baseAlpha = (1f - transparency).coerceIn(0.12f, 0.85f)
    val baseBgColor = Color(0xFF0F1528).copy(alpha = baseAlpha)

    return this
        .shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.7f),
            spotColor = glowColor.copy(alpha = 0.35f)
        )
        .clip(shape)
        .background(baseBgColor)
        .drawBehind {
            // Draw specular reflection gradient at the top edge of the glass card
            val reflectionHeight = size.height * 0.45f
            val reflectionBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = reflectionAlpha * 0.35f),
                    Color.White.copy(alpha = reflectionAlpha * 0.08f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = reflectionHeight
            )
            drawRect(
                brush = reflectionBrush,
                topLeft = Offset.Zero,
                size = size.copy(height = reflectionHeight)
            )

            // Draw subtle bottom-right ambient neon sheen
            val sheenBrush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(size.width, size.height),
                radius = size.width * 0.7f
            )
            drawRect(brush = sheenBrush)
        }
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha * 0.9f),
                    glowColor.copy(alpha = borderAlpha * 0.7f),
                    NeonViolet.copy(alpha = borderAlpha * 0.5f),
                    Color.White.copy(alpha = borderAlpha * 0.2f)
                ),
                start = Offset.Zero,
                end = Offset(400f, 600f)
            ),
            shape = shape
        )
}

/**
 * Applies a tactile 3D Clay UI look with soft double shadows and smooth pill/rounded shapes.
 */
fun Modifier.clayEffect(
    cornerRadius: Dp = 24.dp,
    depth: Dp = 8.dp,
    baseColor: Color = Color(0xFF161B30),
    accentColor: Color = NeonCyan
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = depth,
            shape = shape,
            ambientColor = Color(0xFF04060C).copy(alpha = 0.9f),
            spotColor = accentColor.copy(alpha = 0.25f)
        )
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.92f),
                    Color(0xFF0F1424).copy(alpha = 0.96f)
                ),
                start = Offset(0f, 0f),
                end = Offset(300f, 300f)
            )
        )
        .border(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x556082C5),
                    Color(0x1A23335A)
                ),
                start = Offset(0f, 0f),
                end = Offset(200f, 400f)
            ),
            shape = shape
        )
}

/**
 * Switches between Liquid Glass and Clay UI or Mixed based on settings.
 */
fun Modifier.adaptiveGlassOrClay(
    uiMode: String,
    cornerRadius: Dp = 22.dp,
    transparency: Float = 0.35f,
    glowColor: Color = NeonCyan
): Modifier {
    return when (uiMode) {
        "CLAY" -> this.clayEffect(cornerRadius = cornerRadius, accentColor = glowColor)
        "MIXED" -> this
            .liquidGlass(
                cornerRadius = cornerRadius,
                transparency = (transparency * 0.7f).coerceIn(0.15f, 0.5f),
                glowColor = glowColor
            )
            .clayEffect(
                cornerRadius = cornerRadius,
                depth = 4.dp,
                baseColor = Color(0x331E2642),
                accentColor = glowColor
            )
        else -> this.liquidGlass(
            cornerRadius = cornerRadius,
            transparency = transparency,
            glowColor = glowColor
        )
    }
}
