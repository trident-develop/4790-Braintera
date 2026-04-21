package com.com2us.wannabe.android.google.global.nor.data

import androidx.compose.ui.graphics.Color
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainBlue
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainCoral
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainGreen
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainOrange
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainPurple
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainSky
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainYellow

/**
 * Catalog of mini-games. Each entry carries its own display metadata so the
 * UI layer does not duplicate titles/descriptions across screens.
 */
enum class GameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val goal: String,
    val emoji: String,
    val accent: Color,
    val secondary: Color,
    val totalLevels: Int
) {
    PATTERN_MATCH(
        id = "pattern_match",
        title = "Pattern Match",
        subtitle = "Spot the missing piece",
        description = "A row of colorful pieces hides one missing element. Study the rhythm, notice the repeating idea, and tap the piece that fits.",
        goal = "Complete the pattern by choosing the right element.",
        emoji = "\uD83E\uDDE9", // puzzle
        accent = BrainBlue,
        secondary = BrainSky,
        totalLevels = 23
    ),
    NUMBER_CHAIN(
        id = "number_chain",
        title = "Number Chain",
        subtitle = "Continue the sequence",
        description = "Numbers follow a simple rule — maybe adding, doubling, or alternating. Figure out the trick and pick what comes next.",
        goal = "Select the number that keeps the chain going.",
        emoji = "\uD83D\uDD22", // 1234
        accent = BrainPurple,
        secondary = BrainCoral,
        totalLevels = 23
    ),
    SHAPE_LOGIC(
        id = "shape_logic",
        title = "Shape Logic",
        subtitle = "Pick the next shape",
        description = "Shapes change shape, color or rotation with a rule. Spot the rule and choose the shape that finishes the line.",
        goal = "Choose the shape that fits the rule.",
        emoji = "\uD83D\uDD3A", // red triangle
        accent = BrainCoral,
        secondary = BrainYellow,
        totalLevels = 23
    ),
    MEMORY_GRID(
        id = "memory_grid",
        title = "Memory Grid",
        subtitle = "Remember and repeat",
        description = "A few cells light up in order. When the board goes quiet, tap them back in exactly the same order.",
        goal = "Repeat the highlighted sequence from memory.",
        emoji = "\uD83E\uDDE0", // brain
        accent = BrainGreen,
        secondary = BrainSky,
        totalLevels = 21
    ),
    ODD_ONE_OUT(
        id = "odd_one_out",
        title = "Odd One Out",
        subtitle = "Find what doesn't belong",
        description = "Most items share something — a color, a shape, a direction. One of them breaks the rule. Tap the odd one.",
        goal = "Tap the item that does not match the others.",
        emoji = "\uD83D\uDD0D", // magnifier
        accent = BrainOrange,
        secondary = BrainYellow,
        totalLevels = 23
    );

    companion object {
        val all: List<GameType> = values().toList()
        fun fromId(id: String?): GameType? = values().firstOrNull { it.id == id }
    }
}
