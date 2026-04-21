package com.com2us.wannabe.android.google.global.nor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Braintera is always light/bright. We intentionally ignore system dark mode
// because the design direction calls for a cheerful, colorful look.
private val BrainteraColors = lightColorScheme(
    primary = BrainBlue,
    onPrimary = BrainSurface,
    primaryContainer = BrainPurple,
    onPrimaryContainer = BrainSurface,
    secondary = BrainCoral,
    onSecondary = BrainSurface,
    secondaryContainer = BrainYellow,
    onSecondaryContainer = BrainOnSurface,
    tertiary = BrainGreen,
    onTertiary = BrainSurface,
    background = BrainBackground,
    onBackground = BrainOnSurface,
    surface = BrainSurface,
    onSurface = BrainOnSurface,
    surfaceVariant = BrainBackground,
    onSurfaceVariant = BrainMuted,
    outline = BrainDivider
)

@Composable
fun BrainteraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrainteraColors,
        typography = Typography,
        content = content
    )
}
