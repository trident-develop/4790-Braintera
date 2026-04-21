package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.HintBanner
import com.com2us.wannabe.android.google.global.nor.ui.common.OptionState
import kotlin.random.Random

private data class PatternLevel(
    val pieces: List<String>, // items, one of them is null-marker ("?") for the missing slot
    val missingIndex: Int,
    val options: List<String>,
    val answer: String
) {
    /** Content-only key for dedup; does not include option ordering. */
    val fingerprint: String
        get() = pieces.joinToString("|") + ">" + answer + "@" + missingIndex
}

private val palette = listOf(
    "\uD83C\uDF4E", // apple
    "\uD83C\uDF4B", // lemon
    "\uD83C\uDF47", // grapes
    "\uD83C\uDF53", // strawberry
    "\uD83C\uDF4A", // orange
    "\uD83E\uDED0"  // blueberries
)

private fun generatePatternLevel(levelIndex: Int, seed: Int): PatternLevel {
    val rand = Random(seed * 7919 + 1)
    // Difficulty curve: cycle length grows with level.
    val cycleLength = when {
        levelIndex < 6 -> 2
        levelIndex < 12 -> 3
        else -> 4
    }
    val rowLength = when {
        levelIndex < 6 -> 4
        levelIndex < 12 -> 6
        else -> 8
    }
    val cycle = palette.shuffled(rand).take(cycleLength)
    val row = List(rowLength) { i -> cycle[i % cycleLength] }

    // Choose a missing index that is NOT the first piece so the user has
    // enough context to see the pattern.
    val missingIndex = rand.nextInt(cycleLength, rowLength)
    val answer = row[missingIndex]

    val optionCount = when {
        levelIndex < 6 -> 3
        levelIndex < 18 -> 4
        else -> 5 // hardest tier: more options to pick from
    }
    val distractors = palette.filter { it != answer }.shuffled(rand).take(optionCount - 1)
    val options = (distractors + answer).shuffled(rand)

    return PatternLevel(
        pieces = row.mapIndexed { i, v -> if (i == missingIndex) "?" else v },
        missingIndex = missingIndex,
        options = options,
        answer = answer
    )
}

@Composable
fun PatternMatchGame(
    game: GameType,
    startLevel: Int,
    onLevelPassed: (Int) -> Unit,
    onMistake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelIndex by remember { mutableIntStateOf(startLevel.coerceAtMost(game.totalLevels)) }
    var phase by remember { mutableStateOf(if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing) }
    var pickedOption by remember { mutableStateOf<String?>(null) }
    var attemptTick by remember { mutableIntStateOf(0) }
    val seenKeys = remember { mutableSetOf<String>() }
    val level = remember(levelIndex, attemptTick) {
        if (levelIndex >= game.totalLevels) return@remember null
        generateUniquePuzzle(
            baseSeed = levelIndex * 131 + attemptTick * 911,
            seen = seenKeys,
            generator = { s -> generatePatternLevel(levelIndex, s) },
            fingerprintOf = { it.fingerprint }
        )
    }

    GameHost(
        game = game,
        levelIndex = levelIndex,
        phase = phase,
        onNext = {
            pickedOption = null
            if (phase == Phase.Finished) {
                // Free-play: reshuffle at last level.
                attemptTick++
                phase = Phase.Playing
            } else {
                levelIndex = (levelIndex + 1).coerceAtMost(game.totalLevels)
                phase = if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing
            }
        },
        onRetry = { pickedOption = null; attemptTick++; phase = Phase.Playing },
        onRestartAll = {
            levelIndex = 0; pickedOption = null; attemptTick++
            seenKeys.clear()
            phase = Phase.Playing
        },
        modifier = modifier
    ) {
        if (level == null) {
            CompletedPanel(game)
            return@GameHost
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HintBanner(text = "Which piece completes the pattern?", color = game.accent)
            Spacer(Modifier.height(20.dp))

            PatternRow(level.pieces, level.missingIndex, game.accent)

            Spacer(Modifier.height(28.dp))
            Text(
                "Choose",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            OptionsGrid(
                options = level.options,
                enabled = phase == Phase.Playing,
                pickedOption = pickedOption,
                answer = level.answer,
                accent = game.accent,
                onPick = { picked ->
                    pickedOption = picked
                    if (picked == level.answer) {
                        phase = Phase.Correct
                        onLevelPassed(levelIndex)
                    } else {
                        phase = Phase.Wrong
                        onMistake()
                    }
                }
            )
        }
    }
}

@Composable
private fun PatternRow(pieces: List<String>, missingIndex: Int, accent: Color) {
    // Wrap long rows onto two lines for readability.
    val chunks = if (pieces.size > 5) pieces.chunked((pieces.size + 1) / 2) else listOf(pieces)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        var offset = 0
        for (chunk in chunks) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEachIndexed { i, piece ->
                    val absoluteIndex = offset + i
                    PatternCell(piece = piece, isMissing = absoluteIndex == missingIndex, accent = accent)
                }
            }
            offset += chunk.size
        }
    }
}

@Composable
private fun PatternCell(piece: String, isMissing: Boolean, accent: Color) {
    val bg = if (isMissing) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isMissing) {
            Text("?", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(piece, fontSize = 26.sp)
        }
    }
}

@Composable
private fun OptionsGrid(
    options: List<String>,
    enabled: Boolean,
    pickedOption: String?,
    answer: String,
    accent: Color,
    onPick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val state = when {
                pickedOption == null -> OptionState.Idle
                option == answer && pickedOption == option -> OptionState.Correct
                option == pickedOption -> OptionState.Wrong
                else -> OptionState.Idle
            }
            OptionEmoji(
                emoji = option,
                state = state,
                enabled = enabled && pickedOption == null,
                accent = accent,
                modifier = Modifier.weight(1f),
                onClick = { onPick(option) }
            )
        }
    }
}

@Composable
internal fun OptionEmoji(
    emoji: String,
    state: OptionState,
    enabled: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when (state) {
        OptionState.Idle -> MaterialTheme.colorScheme.surface
        OptionState.Correct -> Color(0xFF2BD99F)
        OptionState.Wrong -> Color(0xFFFF6B6B)
    }
    val scale by animateFloatAsState(if (state != OptionState.Idle) 1.06f else 1f, tween(200), label = "opt-emoji")

    Box(
        modifier = modifier
            .height(72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(8.dp)
            .then(if (enabled) Modifier.clickableNoIndication(onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 28.sp)
    }
    @Suppress("UNUSED_EXPRESSION") accent
}

@Composable
internal fun CompletedPanel(game: GameType) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("\uD83C\uDF89", fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "All ${game.totalLevels} levels complete!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "You mastered ${game.title}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Clickable that disables the ripple for a cleaner look. */
@Composable
internal fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}
