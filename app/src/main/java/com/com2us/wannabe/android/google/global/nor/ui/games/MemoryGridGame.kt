package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.HintBanner
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class MemoryLevel(
    val gridSize: Int,      // 3 or 4
    val sequence: List<Int> // cell indices in order
) {
    val fingerprint: String get() = "$gridSize:" + sequence.joinToString(",")
}

private fun generateMemoryLevel(levelIndex: Int, seed: Int): MemoryLevel {
    val rand = Random(seed * 4409 + 7)
    val gridSize = if (levelIndex < 12) 3 else 4
    val cellCount = gridSize * gridSize
    val sequenceLen = when {
        levelIndex < 4 -> 3
        levelIndex < 8 -> 4
        levelIndex < 12 -> 5
        levelIndex < 17 -> 6
        else -> 7
    }.coerceAtMost(cellCount)

    val pool = (0 until cellCount).toMutableList()
    val seq = mutableListOf<Int>()
    repeat(sequenceLen) {
        // Allow reuse only if pool empties — with current sizes it never will.
        if (pool.isEmpty()) pool.addAll((0 until cellCount))
        val pick = pool.random(rand)
        pool.remove(pick)
        seq += pick
    }
    return MemoryLevel(gridSize, seq)
}

private enum class Stage { Showing, Recall, Reveal }

@Composable
fun MemoryGridGame(
    game: GameType,
    startLevel: Int,
    onLevelPassed: (Int) -> Unit,
    onMistake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelIndex by remember { mutableIntStateOf(startLevel.coerceAtMost(game.totalLevels)) }
    var phase by remember { mutableStateOf(if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing) }
    var attemptTick by remember { mutableIntStateOf(0) }
    val seenKeys = remember { mutableSetOf<String>() }
    val level = remember(levelIndex, attemptTick) {
        if (levelIndex >= game.totalLevels) return@remember null
        generateUniquePuzzle(
            baseSeed = levelIndex * 83 + attemptTick * 691,
            seen = seenKeys,
            generator = { s -> generateMemoryLevel(levelIndex, s) },
            fingerprintOf = { it.fingerprint }
        )
    }

    // Per-level stage state
    var stage by remember(levelIndex, attemptTick) { mutableStateOf(Stage.Showing) }
    var highlightedCell by remember(levelIndex, attemptTick) { mutableStateOf<Int?>(null) }
    var userProgress by remember(levelIndex, attemptTick) { mutableIntStateOf(0) }
    var lastTappedCell by remember(levelIndex, attemptTick) { mutableStateOf<Int?>(null) }
    var lastTappedCorrect by remember(levelIndex, attemptTick) { mutableStateOf<Boolean?>(null) }

    // Playback of sequence
    LaunchedEffect(levelIndex, attemptTick) {
        if (level == null) return@LaunchedEffect
        stage = Stage.Showing
        delay(500)
        level.sequence.forEach { idx ->
            highlightedCell = idx
            delay(520)
            highlightedCell = null
            delay(180)
        }
        stage = Stage.Recall
    }

    GameHost(
        game = game,
        levelIndex = levelIndex,
        phase = phase,
        onNext = {
            if (phase == Phase.Finished) {
                attemptTick++
                phase = Phase.Playing
            } else {
                levelIndex = (levelIndex + 1).coerceAtMost(game.totalLevels)
                phase = if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing
                attemptTick++
            }
        },
        onRetry = {
            attemptTick++
            phase = Phase.Playing
        },
        onRestartAll = {
            levelIndex = 0
            attemptTick++
            seenKeys.clear()
            phase = Phase.Playing
        },
        modifier = modifier
    ) {
        if (level == null) { CompletedPanel(game); return@GameHost }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HintBanner(
                text = when (stage) {
                    Stage.Showing -> "Watch the order the cells light up"
                    Stage.Recall -> "Now tap them in the same order"
                    Stage.Reveal -> "Here is the correct order"
                },
                color = game.accent
            )
            Spacer(Modifier.height(20.dp))
            MemoryGrid(
                gridSize = level.gridSize,
                highlightedCell = highlightedCell,
                accent = game.accent,
                lastTappedCell = lastTappedCell,
                lastTappedCorrect = lastTappedCorrect,
                enabled = stage == Stage.Recall,
                onCellTap = { cellIdx ->
                    if (stage != Stage.Recall) return@MemoryGrid
                    val expected = level.sequence[userProgress]
                    lastTappedCell = cellIdx
                    lastTappedCorrect = (cellIdx == expected)
                    if (cellIdx == expected) {
                        userProgress += 1
                        if (userProgress >= level.sequence.size) {
                            phase = Phase.Correct
                            onLevelPassed(levelIndex)
                        }
                    } else {
                        phase = Phase.Wrong
                        onMistake()
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            ProgressDots(total = level.sequence.size, done = userProgress, accent = game.accent)
        }
    }
}

@Composable
private fun MemoryGrid(
    gridSize: Int,
    highlightedCell: Int?,
    accent: Color,
    lastTappedCell: Int?,
    lastTappedCorrect: Boolean?,
    enabled: Boolean,
    onCellTap: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (row in 0 until gridSize) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0 until gridSize) {
                    val idx = row * gridSize + col
                    val isLit = highlightedCell == idx
                    val tappedOk = lastTappedCell == idx && lastTappedCorrect == true
                    val tappedBad = lastTappedCell == idx && lastTappedCorrect == false

                    val color = when {
                        isLit -> accent
                        tappedOk -> Color(0xFF2BD99F)
                        tappedBad -> Color(0xFFFF6B6B)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val animated by animateColorAsState(color, tween(180), label = "mem-cell")

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(animated)
                            .then(if (enabled) Modifier.clickableNoIndication { onCellTap(idx) } else Modifier)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressDots(total: Int, done: Int, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val c = if (i < done) accent else accent.copy(alpha = 0.25f)
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(c)
            )
        }
    }
}
