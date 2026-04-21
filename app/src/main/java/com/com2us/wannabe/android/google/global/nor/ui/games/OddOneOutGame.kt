package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.HintBanner
import com.com2us.wannabe.android.google.global.nor.ui.common.OptionState
import kotlin.random.Random

/**
 * Either a colored tile or an emoji tile. Represented as a sealed-like struct
 * to keep rendering logic simple.
 */
private data class OddTile(
    val emoji: String? = null,
    val color: Color? = null
)

private data class OddLevel(
    val tiles: List<OddTile>,
    val gridCols: Int,
    val oddIndex: Int,
    val rule: String
) {
    val fingerprint: String
        get() = tiles.joinToString("|") { t ->
            (t.emoji ?: "") + "#" + (t.color?.value?.toLong() ?: 0L)
        } + "@" + oddIndex
}

private val emojiGroups: List<List<String>> = listOf(
    // Fruit
    listOf("\uD83C\uDF4E", "\uD83C\uDF4B", "\uD83C\uDF47", "\uD83C\uDF53", "\uD83C\uDF4A", "\uD83C\uDF4C"),
    // Animals land
    listOf("\uD83D\uDC31", "\uD83D\uDC36", "\uD83D\uDC2F", "\uD83D\uDC2E", "\uD83D\uDC37"),
    // Animals sea
    listOf("\uD83D\uDC20", "\uD83D\uDC19", "\uD83E\uDD80", "\uD83D\uDC1F"),
    // Vehicles
    listOf("\uD83D\uDE97", "\uD83D\uDE8C", "\uD83D\uDEB2", "\uD83D\uDEF4", "\uD83C\uDFCD"),
    // Sports balls
    listOf("\u26BD", "\uD83C\uDFC0", "\u26BE", "\uD83C\uDFBE", "\uD83C\uDFD0")
)

private val tileColors = listOf(
    Color(0xFF4A6CF7), Color(0xFFFF6B9D), Color(0xFFFFC857),
    Color(0xFF2BD99F), Color(0xFF8B5CF6), Color(0xFFFF8A5C), Color(0xFF56CCF2)
)

private fun generateOddLevel(levelIndex: Int, seed: Int): OddLevel {
    val rand = Random(seed * 5923 + 3)

    val gridCols: Int
    val total: Int
    when {
        levelIndex < 4 -> { gridCols = 2; total = 4 }
        levelIndex < 11 -> { gridCols = 3; total = 6 }
        else -> { gridCols = 3; total = 9 }
    }

    return when {
        levelIndex < 8 -> {
            // Color based: all tiles one color except one.
            val base = tileColors.random(rand)
            val odd = tileColors.filter { it != base }.random(rand)
            val oddIdx = rand.nextInt(total)
            val tiles = (0 until total).map { i -> OddTile(color = if (i == oddIdx) odd else base) }
            OddLevel(tiles, gridCols, oddIdx, "Find the tile with a different color")
        }
        levelIndex < 15 -> {
            // Category based: all from one group, one from a different group.
            val group = emojiGroups.random(rand)
            val other = emojiGroups.filter { it != group }.random(rand)
            val oddIdx = rand.nextInt(total)
            val tiles = (0 until total).map { i ->
                val source = if (i == oddIdx) other else group
                OddTile(emoji = source.random(rand))
            }
            OddLevel(tiles, gridCols, oddIdx, "One item does not belong to the group")
        }
        else -> {
            // Same emoji except one different emoji from SAME group (harder).
            val group = emojiGroups.filter { it.size >= 2 }.random(rand)
            val match = group.random(rand)
            val different = group.filter { it != match }.random(rand)
            val oddIdx = rand.nextInt(total)
            val tiles = (0 until total).map { i ->
                OddTile(emoji = if (i == oddIdx) different else match)
            }
            OddLevel(tiles, gridCols, oddIdx, "One item is slightly different — look closely")
        }
    }
}

@Composable
fun OddOneOutGame(
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
            baseSeed = levelIndex * 113 + attemptTick * 619,
            seen = seenKeys,
            generator = { s -> generateOddLevel(levelIndex, s) },
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
            HintBanner(level.rule, color = game.accent)
            Spacer(Modifier.height(20.dp))
            TileGrid(
                level = level,
                picked = picked,
                enabled = phase == Phase.Playing,
                onPick = { idx ->
                    picked = idx
                    if (idx == level.oddIndex) {
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
private fun TileGrid(
    level: OddLevel,
    picked: Int?,
    enabled: Boolean,
    onPick: (Int) -> Unit
) {
    val rows = (level.tiles.size + level.gridCols - 1) / level.gridCols
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (c in 0 until level.gridCols) {
                    val idx = r * level.gridCols + c
                    if (idx >= level.tiles.size) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val tile = level.tiles[idx]
                        val state = when {
                            picked == null -> OptionState.Idle
                            idx == level.oddIndex && picked == idx -> OptionState.Correct
                            idx == picked -> OptionState.Wrong
                            else -> OptionState.Idle
                        }
                        OddTileView(
                            tile = tile,
                            state = state,
                            enabled = enabled && picked == null,
                            onClick = { onPick(idx) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OddTileView(
    tile: OddTile,
    state: OptionState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        if (state != OptionState.Idle) 1.06f else 1f,
        tween(200), label = "odd-scale"
    )
    val overlay = when (state) {
        OptionState.Correct -> Color(0xFF2BD99F).copy(alpha = 0.85f)
        OptionState.Wrong -> Color(0xFFFF6B6B).copy(alpha = 0.85f)
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .height(88.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(tile.color ?: MaterialTheme.colorScheme.surface)
            .then(if (enabled) Modifier.clickableNoIndication(onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (tile.emoji != null) Text(tile.emoji, fontSize = 34.sp)
        if (overlay != Color.Transparent) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(overlay),
                contentAlignment = Alignment.Center
            ) {
                Text(if (state == OptionState.Correct) "✓" else "✕", color = Color.White, fontSize = 22.sp)
            }
        }
    }
}
