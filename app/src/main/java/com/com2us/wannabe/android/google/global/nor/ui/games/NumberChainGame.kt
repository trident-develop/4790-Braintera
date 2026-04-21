package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.HintBanner
import com.com2us.wannabe.android.google.global.nor.ui.common.OptionState
import kotlin.math.abs
import kotlin.random.Random

private data class NumberChainLevel(
    val visible: List<Int>,
    val answer: Int,
    val options: List<Int>,
    val rule: String
) {
    val fingerprint: String get() = visible.joinToString(",") + ">" + answer
}

private fun generateNumberLevel(levelIndex: Int, seed: Int): NumberChainLevel {
    val rand = Random(seed * 3217 + 11)

    // Pick a rule that matches the difficulty bucket.
    val rule: (Int) -> Int
    val ruleLabel: String
    val start: Int
    val length = 4

    when {
        levelIndex < 5 -> {
            val step = rand.nextInt(2, 5)
            start = rand.nextInt(1, 12)
            rule = { i -> start + step * i }
            ruleLabel = "Add $step each step"
        }
        levelIndex < 10 -> {
            val step = rand.nextInt(2, 6)
            start = rand.nextInt(30, 70)
            rule = { i -> start - step * i }
            ruleLabel = "Subtract $step each step"
        }
        levelIndex < 14 -> {
            // Alternating +a, +b
            val a = rand.nextInt(1, 5)
            val b = rand.nextInt(1, 6)
            start = rand.nextInt(1, 8)
            rule = { i ->
                var v = start
                repeat(i) { k -> v += if (k % 2 == 0) a else b }
                v
            }
            ruleLabel = "Alternating +$a, +$b"
        }
        levelIndex < 18 -> {
            // Doubling
            start = rand.nextInt(1, 4)
            rule = { i -> start * (1 shl i) }
            ruleLabel = "Double each step"
        }
        else -> {
            // Fibonacci-like
            val a0 = rand.nextInt(1, 4)
            val a1 = rand.nextInt(1, 5) + a0
            rule = { i ->
                if (i == 0) a0
                else if (i == 1) a1
                else {
                    var prev = a0; var cur = a1
                    repeat(i - 1) { val n = prev + cur; prev = cur; cur = n }
                    cur
                }
            }
            start = a0
            ruleLabel = "Each number = sum of previous two"
        }
    }

    val visible = (0 until length).map(rule)
    val answer = rule(length)

    // Build options with plausible-but-wrong distractors.
    val wrong = mutableSetOf<Int>()
    while (wrong.size < 3) {
        val delta = listOf(-3, -2, -1, 1, 2, 3, 4).random(rand)
        val cand = answer + delta
        if (cand != answer && cand > 0 && visible.none { it == cand } &&
            abs(cand - answer) >= 1
        ) {
            wrong += cand
        }
    }
    val options = (wrong + answer).toList().shuffled(rand)
    return NumberChainLevel(visible, answer, options, ruleLabel)
}

@Composable
fun NumberChainGame(
    game: GameType,
    startLevel: Int,
    onLevelPassed: (Int) -> Unit,
    onMistake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelIndex by remember { mutableIntStateOf(startLevel.coerceAtMost(game.totalLevels)) }
    var phase by remember { mutableStateOf(if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var attemptTick by remember { mutableIntStateOf(0) }
    val seenKeys = remember { mutableSetOf<String>() }
    val level = remember(levelIndex, attemptTick) {
        if (levelIndex >= game.totalLevels) return@remember null
        generateUniquePuzzle(
            baseSeed = levelIndex * 97 + attemptTick * 733,
            seen = seenKeys,
            generator = { s -> generateNumberLevel(levelIndex, s) },
            fingerprintOf = { it.fingerprint }
        )
    }

    GameHost(
        game = game,
        levelIndex = levelIndex,
        phase = phase,
        onNext = {
            picked = null
            if (phase == Phase.Finished) { attemptTick++; phase = Phase.Playing }
            else {
                levelIndex = (levelIndex + 1).coerceAtMost(game.totalLevels)
                phase = if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing
            }
        },
        onRetry = { picked = null; attemptTick++; phase = Phase.Playing },
        onRestartAll = {
            levelIndex = 0; picked = null; attemptTick++
            seenKeys.clear()
            phase = Phase.Playing
        },
        modifier = modifier
    ) {
        if (level == null) { CompletedPanel(game); return@GameHost }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HintBanner(text = "What number comes next?", color = game.accent)
            Spacer(Modifier.height(20.dp))
            NumberSequence(level.visible, game.accent)
            Spacer(Modifier.height(28.dp))
            Text(
                "Pick the next number",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            NumberOptionsRow(
                options = level.options,
                picked = picked,
                answer = level.answer,
                enabled = phase == Phase.Playing,
                accent = game.accent,
                onPick = { p ->
                    picked = p
                    if (p == level.answer) {
                        phase = Phase.Correct; onLevelPassed(levelIndex)
                    } else {
                        phase = Phase.Wrong; onMistake()
                    }
                }
            )
        }
    }
}

@Composable
private fun NumberSequence(visible: List<Int>, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        visible.forEach { NumberChip(it.toString(), accent, missing = false) }
        NumberChip("?", accent, missing = true)
    }
}

@Composable
private fun NumberChip(text: String, accent: Color, missing: Boolean) {
    val bg = if (missing) accent.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surface
    val fg = if (missing) accent else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NumberOptionsRow(
    options: List<Int>,
    picked: Int?,
    answer: Int,
    enabled: Boolean,
    accent: Color,
    onPick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { opt ->
            val state = when {
                picked == null -> OptionState.Idle
                opt == answer && picked == opt -> OptionState.Correct
                opt == picked -> OptionState.Wrong
                else -> OptionState.Idle
            }
            NumberOption(
                text = opt.toString(),
                state = state,
                enabled = enabled && picked == null,
                accent = accent,
                onClick = { onPick(opt) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumberOption(
    text: String,
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
    val fg = if (state == OptionState.Idle) MaterialTheme.colorScheme.onSurface else Color.White

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .then(if (enabled) Modifier.clickableNoIndication(onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    @Suppress("UNUSED_EXPRESSION") accent
}
