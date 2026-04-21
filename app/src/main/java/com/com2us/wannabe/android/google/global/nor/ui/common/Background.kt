package com.com2us.wannabe.android.google.global.nor.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainBackground
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainCoral
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainPurple
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainSky
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainYellow

/**
 * A soft, animated radial-glow background used as the root of every screen.
 * Two slow-drifting color blobs give the UI a subtle, living feel without
 * costing much performance.
 */
@Composable
fun PlayfulBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "bg-t"
    )

    Box(modifier = modifier.fillMaxSize().background(BrainBackground)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawBlob(
                center = Offset(w * (0.2f + 0.15f * t), h * (0.18f + 0.08f * t)),
                radius = w * 0.9f,
                color = BrainPurple.copy(alpha = 0.22f)
            )
            drawBlob(
                center = Offset(w * (0.85f - 0.1f * t), h * (0.35f + 0.1f * t)),
                radius = w * 0.85f,
                color = BrainSky.copy(alpha = 0.25f)
            )
            drawBlob(
                center = Offset(w * (0.25f + 0.1f * t), h * (0.9f - 0.1f * t)),
                radius = w * 0.9f,
                color = BrainCoral.copy(alpha = 0.18f)
            )
            drawBlob(
                center = Offset(w * (0.8f - 0.1f * t), h * (0.85f + 0.05f * t)),
                radius = w * 0.7f,
                color = BrainYellow.copy(alpha = 0.20f)
            )
        }
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlob(
    center: Offset,
    radius: Float,
    color: Color
) {
    val brush = ShaderBrush(
        RadialGradientShader(
            center = center,
            radius = radius,
            colors = listOf(color, Color.Transparent),
            colorStops = listOf(0f, 1f)
        )
    )
    drawRect(brush = brush, topLeft = Offset.Zero, size = Size(size.width, size.height))
}

/** Handy linear brush using the two accent colors of a game. */
fun accentGradient(primary: Color, secondary: Color): Brush =
    Brush.linearGradient(colors = listOf(primary, secondary))
