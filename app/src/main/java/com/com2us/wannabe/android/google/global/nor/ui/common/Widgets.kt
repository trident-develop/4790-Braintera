package com.com2us.wannabe.android.google.global.nor.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Rounded white card with a soft shadow. */
@Composable
fun PlayfulCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24,
    elevation: Int = 10,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val base = modifier
        .shadow(elevation.dp, shape, clip = false, ambientColor = Color(0x14000000), spotColor = Color(0x1F000000))
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)

    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(120), label = "card-scale")
        Box(
            modifier = base
                .scale(scale)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
        ) { content() }
    } else {
        Box(modifier = base) { content() }
    }
}

/** Gradient pill-shaped primary button. */
@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(120), label = "btn-scale")
    val alpha by animateFloatAsState(if (enabled) 1f else 0.5f, tween(150), label = "btn-alpha")

    Box(
        modifier = modifier
            .heightIn(min = 54.dp)
            .fillMaxWidth()
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(18.dp), clip = false, spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = alpha),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp)
        )
    }
}

/** Flat ghost button for secondary actions. */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, color),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/** Slim rounded progress bar. */
@Composable
fun SlimProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outline
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(clamped, tween(500), label = "progress")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(trackColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(10.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/** Large colored pill used as an answer-option button. */
@Composable
fun OptionPill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    state: OptionState = OptionState.Idle,
    onClick: () -> Unit
) {
    val targetBg by animateColorAsState(
        when (state) {
            OptionState.Idle -> background
            OptionState.Correct -> Color(0xFF2BD99F)
            OptionState.Wrong -> Color(0xFFFF6B6B)
        },
        tween(220), label = "opt-bg"
    )
    val targetText by animateColorAsState(
        when (state) {
            OptionState.Idle -> textColor
            else -> Color.White
        },
        tween(220), label = "opt-text"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(120), label = "opt-scale")

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(20.dp), clip = false, spotColor = Color(0x22000000))
            .clip(RoundedCornerShape(20.dp))
            .background(targetBg)
            .clickable(
                enabled = state == OptionState.Idle,
                interactionSource = interaction,
                indication = null
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = targetText,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

enum class OptionState { Idle, Correct, Wrong }

/** Small chip that shows "Level X · Y%" style info. */
@Composable
fun InfoChip(label: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Colored round badge with an emoji (or short text) inside. */
@Composable
fun EmojiBadge(
    text: String,
    background: Brush,
    size: Int = 56,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(6.dp, CircleShape, clip = false, spotColor = Color(0x33000000))
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = (size * 0.45).sp,
            textAlign = TextAlign.Center,
            color = Color.White
        )
    }
}

@Composable
fun HSpace(width: Int) = Spacer(Modifier.width(width.dp))

@Composable
fun VSpace(height: Int) = Spacer(Modifier.height(height.dp))

/** Semi-transparent inline banner used for explanation text. */
@Composable
fun HintBanner(text: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("\uD83D\uDCA1", fontSize = 18.sp) // lightbulb
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
