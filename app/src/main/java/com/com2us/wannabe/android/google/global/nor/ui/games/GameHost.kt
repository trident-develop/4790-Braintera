package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.GhostButton
import com.com2us.wannabe.android.google.global.nor.ui.common.GradientButton
import com.com2us.wannabe.android.google.global.nor.ui.common.InfoChip
import com.com2us.wannabe.android.google.global.nor.ui.common.SlimProgress
import com.com2us.wannabe.android.google.global.nor.ui.common.accentGradient

/**
 * Phase of play for a single level.
 */
enum class Phase { Playing, Correct, Wrong, Finished }

/**
 * Shared scaffolding used by every mini-game. It paints the title + level +
 * progress, hosts the [playArea] composable from the concrete game, and shows
 * the appropriate Next/Retry/Replay controls depending on [phase].
 */
@Composable
fun GameHost(
    game: GameType,
    levelIndex: Int,
    phase: Phase,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onRestartAll: () -> Unit,
    modifier: Modifier = Modifier,
    playArea: @Composable () -> Unit
) {
    val gradient = accentGradient(game.accent, game.secondary)
    val totalLevels = game.totalLevels
    val displayLevel = (levelIndex + 1).coerceAtMost(totalLevels)
    val progress =
        if (phase == Phase.Finished) 1f
        else (levelIndex.toFloat() / totalLevels).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) { Text(game.emoji, fontSize = 24.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    game.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (phase == Phase.Finished) "All levels complete"
                    else "Level $displayLevel / $totalLevels",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            InfoChip(label = "${(progress * 100).toInt()}%", color = game.accent)
        }

        Spacer(Modifier.height(12.dp))
        SlimProgress(progress = progress, color = game.accent)
        Spacer(Modifier.height(20.dp))

        // Play area fills the middle.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            playArea()
        }

        Spacer(Modifier.height(12.dp))

        // Footer controls
        when (phase) {
            Phase.Playing -> {
                // No permanent button: the game itself reacts to taps.
                Spacer(Modifier.height(8.dp))
            }
            Phase.Correct -> {
                FeedbackBadge(
                    text = "Great!",
                    emoji = "\uD83C\uDF89",
                    color = Color(0xFF2BD99F)
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(text = "Next level", gradient = gradient, onClick = onNext)
            }
            Phase.Wrong -> {
                FeedbackBadge(
                    text = "Not quite — try again",
                    emoji = "\uD83D\uDE22",
                    color = Color(0xFFFF6B6B)
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(text = "Retry", gradient = gradient, onClick = onRetry)
            }
            Phase.Finished -> {
                FeedbackBadge(
                    text = "Game complete!",
                    emoji = "\uD83C\uDFC6",
                    color = game.accent
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(text = "Play again from level 1", gradient = gradient, onClick = onRestartAll)
                Spacer(Modifier.height(10.dp))
                GhostButton(text = "Continue free play", color = game.accent, onClick = onNext)
            }
        }
    }
}

@Composable
private fun FeedbackBadge(text: String, emoji: String, color: Color) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.85f),
        exit = fadeOut(tween(120))
    ) {
        val pulse = rememberInfiniteTransition(label = "pulse")
        val s by pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "pulse-scale"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(s)
                .clip(RoundedCornerShape(18.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
            Text(
                text,
                color = color,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** Gentle no-op tap blocker for "locked" states. */
@Composable
fun TapGuard(enabled: Boolean, content: @Composable () -> Unit) {
    if (enabled) {
        Box(Modifier.fillMaxSize().clickable(enabled = true, onClick = {})) { content() }
    } else {
        content()
    }
}

/** Helper so game composables can paint a soft accent-tinted card behind content. */
@Composable
fun AccentPanel(
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) { content() }
    }
    @Suppress("UNUSED_EXPRESSION") gradient
}
