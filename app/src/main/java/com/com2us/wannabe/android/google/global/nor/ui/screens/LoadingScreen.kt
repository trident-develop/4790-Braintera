package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.ui.common.PlayfulBackground
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainBlue
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainCoral
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainGreen
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainPurple
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainSky
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The splash for [LoadingActivity]. Shows:
 *  - an animated brain / neural-node glyph
 *  - an infinite progress bar (a "thinking dots" glowing strip)
 *  - the app name and tagline
 */
@Composable
fun LoadingScreen() {
    PlayfulBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedBrainEmblem()
            Spacer(Modifier.height(36.dp))
            Text(
                "Braintera",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Warming up your neurons",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(36.dp))
            InfiniteProgressBar()
        }
    }
}

@Composable
private fun AnimatedBrainEmblem() {
    val transition = rememberInfiniteTransition(label = "emblem")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "spin"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating orbit of colored nodes.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(spin)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f
            val nodeColors = listOf(BrainBlue, BrainCoral, BrainGreen, BrainYellow, BrainPurple, BrainSky)
            val steps = nodeColors.size

            // Connecting ring
            drawCircle(
                color = BrainBlue.copy(alpha = 0.15f),
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Nodes around the circumference
            for (i in 0 until steps) {
                val angle = (i * 2.0 * PI / steps).toFloat()
                val nx = center.x + radius * cos(angle)
                val ny = center.y + radius * sin(angle)
                drawCircle(
                    color = nodeColors[i],
                    radius = 10.dp.toPx(),
                    center = Offset(nx, ny)
                )
                drawCircle(
                    color = nodeColors[i].copy(alpha = 0.25f),
                    radius = 16.dp.toPx(),
                    center = Offset(nx, ny)
                )
            }

            // Inner spokes connecting nodes through the center for a "wired brain" look.
            for (i in 0 until steps) {
                val angle = (i * 2.0 * PI / steps).toFloat()
                val nx = center.x + radius * cos(angle)
                val ny = center.y + radius * sin(angle)
                drawLine(
                    color = nodeColors[i].copy(alpha = 0.35f),
                    start = center,
                    end = Offset(nx, ny),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center glowing brain disc
        Box(
            modifier = Modifier
                .size(108.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrainBlue, BrainPurple, BrainCoral)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("\uD83E\uDDE0", fontSize = 54.sp)
        }
    }
}

@Composable
private fun InfiniteProgressBar() {
    val transition = rememberInfiniteTransition(label = "bar")
    val t by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "bar-t"
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val headGradient = Brush.horizontalGradient(
        colors = listOf(BrainBlue, BrainPurple, BrainCoral, BrainYellow)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val segmentWidth = w * 0.45f
            val x = t * (w + segmentWidth) - segmentWidth
            drawRoundRect(
                brush = headGradient,
                topLeft = Offset(x, 0f),
                size = Size(segmentWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
            )
        }
    }
}
