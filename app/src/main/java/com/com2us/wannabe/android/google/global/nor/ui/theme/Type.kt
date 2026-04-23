package com.com2us.wannabe.android.google.global.nor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.microdose.ball.R

// Project-provided font, used across the whole app.
val BrainteraFont = FontFamily(
    Font(R.font.font, FontWeight.Normal),
    Font(R.font.font, FontWeight.Medium),
    Font(R.font.font, FontWeight.SemiBold),
    Font(R.font.font, FontWeight.Bold)
)

private fun base(
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    line: Int = size + 6,
    spacing: Double = 0.2
) = TextStyle(
    fontFamily = BrainteraFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

val Typography = Typography(
    displayLarge = base(40, FontWeight.Bold, 48),
    displayMedium = base(34, FontWeight.Bold, 42),
    displaySmall = base(28, FontWeight.Bold, 36),
    headlineLarge = base(26, FontWeight.Bold, 32),
    headlineMedium = base(22, FontWeight.SemiBold, 28),
    headlineSmall = base(20, FontWeight.SemiBold, 26),
    titleLarge = base(20, FontWeight.SemiBold, 26),
    titleMedium = base(17, FontWeight.SemiBold, 22),
    titleSmall = base(15, FontWeight.SemiBold, 20),
    bodyLarge = base(16, FontWeight.Normal, 22),
    bodyMedium = base(14, FontWeight.Normal, 20),
    bodySmall = base(12, FontWeight.Normal, 16),
    labelLarge = base(14, FontWeight.SemiBold, 18, 0.4),
    labelMedium = base(12, FontWeight.Medium, 16, 0.4),
    labelSmall = base(11, FontWeight.Medium, 14, 0.5)
)
