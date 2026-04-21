package com.com2us.wannabe.android.google.global.nor.ui.games

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.HintBanner
import com.com2us.wannabe.android.google.global.nor.ui.common.OptionState
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainBlue
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainCoral
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainGreen
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainPurple
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainYellow
import kotlin.random.Random

enum class ShapeKind { Circle, Square, Triangle, Diamond }

data class ShapeSpec(val kind: ShapeKind, val color: Color, val rotationDeg: Float = 0f)

private data class ShapeLevel(
    val sequence: List<ShapeSpec>,
    val answer: ShapeSpec,
    val options: List<ShapeSpec>,
    val rule: String
) {
    val fingerprint: String
        get() = sequence.joinToString("|") { specKey(it) } + ">" + specKey(answer)
}

private fun specKey(s: ShapeSpec): String = "${s.kind}:${s.color.value.toLong()}:${s.rotationDeg.toInt()}"

private val shapeColors = listOf(BrainBlue, BrainCoral, BrainGreen, BrainYellow, BrainPurple)
private val kinds = ShapeKind.values().toList()

private fun generateShapeLevel(levelIndex: Int, seed: Int): ShapeLevel {
    val rand = Random(seed * 2389 + 5)

    val sequence: List<ShapeSpec>
    val answer: ShapeSpec
    val rule: String

    when {
        levelIndex < 5 -> {
            // Two alternating colors, same kind.
            val kind = kinds.random(rand)
            val c1 = shapeColors.random(rand)
            val c2 = shapeColors.filter { it != c1 }.random(rand)
            val cols = listOf(c1, c2, c1, c2)
            sequence = cols.map { ShapeSpec(kind, it) }
            answer = ShapeSpec(kind, c1)
            rule = "Colors repeat in a pair"
        }
        levelIndex < 10 -> {
            // Cycling kinds, same color.
            val color = shapeColors.random(rand)
            val ks = kinds.shuffled(rand).take(3)
            val ord = listOf(ks[0], ks[1], ks[2], ks[0])
            sequence = ord.map { ShapeSpec(it, color) }
            answer = ShapeSpec(ks[1], color)
            rule = "Shapes repeat in a cycle of three"
        }
        levelIndex < 14 -> {
            // Kinds and colors both rotating pair-wise.
            val ks = kinds.shuffled(rand).take(2)
            val cs = shapeColors.shuffled(rand).take(2)
            sequence = listOf(
                ShapeSpec(ks[0], cs[0]),
                ShapeSpec(ks[1], cs[1]),
                ShapeSpec(ks[0], cs[0]),
                ShapeSpec(ks[1], cs[1])
            )
            answer = ShapeSpec(ks[0], cs[0])
            rule = "Each row repeats as a pair"
        }
        levelIndex < 19 -> {
            // Rotation: 0, 90, 180, 270 style with same kind and color.
            val kind = if (rand.nextBoolean()) ShapeKind.Triangle else ShapeKind.Diamond
            val color = shapeColors.random(rand)
            val rots = listOf(0f, 90f, 180f, 270f)
            sequence = rots.map { ShapeSpec(kind, color, it) }
            answer = ShapeSpec(kind, color, 0f)
            rule = "Rotation advances by 90°"
        }
        else -> {
            // Hardest: rotation AND alternating color.
            val kind = if (rand.nextBoolean()) ShapeKind.Triangle else ShapeKind.Diamond
            val cs = shapeColors.shuffled(rand).take(2)
            val rots = listOf(0f, 90f, 180f, 270f)
            sequence = rots.mapIndexed { i, r -> ShapeSpec(kind, cs[i % 2], r) }
            answer = ShapeSpec(kind, cs[0], 0f) // next full cycle back to start
            rule = "Rotation + colors alternate together"
        }
    }

    // Build distractors.
    val distractors = mutableListOf<ShapeSpec>()
    while (distractors.size < 3) {
        val cand = ShapeSpec(
            kind = kinds.random(rand),
            color = shapeColors.random(rand),
            rotationDeg = listOf(0f, 90f, 180f, 270f).random(rand)
        )
        val already = (distractors + listOf(answer)).any {
            it.kind == cand.kind && it.color == cand.color && it.rotationDeg == cand.rotationDeg
        }
        if (!already) distractors += cand
    }
    val options = (distractors + answer).shuffled(rand)
    return ShapeLevel(sequence, answer, options, rule)
}

@Composable
fun ShapeLogicGame(
    game: GameType,
    startLevel: Int,
    onLevelPassed: (Int) -> Unit,
    onMistake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var levelIndex by remember { mutableIntStateOf(startLevel.coerceAtMost(game.totalLevels)) }
    var phase by remember { mutableStateOf(if (levelIndex >= game.totalLevels) Phase.Finished else Phase.Playing) }
    var picked by remember { mutableStateOf<ShapeSpec?>(null) }
    var attemptTick by remember { mutableIntStateOf(0) }
    val seenKeys = remember { mutableSetOf<String>() }
    val level = remember(levelIndex, attemptTick) {
        if (levelIndex >= game.totalLevels) return@remember null
        generateUniquePuzzle(
            baseSeed = levelIndex * 59 + attemptTick * 547,
            seen = seenKeys,
            generator = { s -> generateShapeLevel(levelIndex, s) },
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
            HintBanner(text = level.rule, color = game.accent)
            Spacer(Modifier.height(20.dp))
            ShapeSequenceRow(level.sequence, accent = game.accent)
            Spacer(Modifier.height(28.dp))
            Text(
                "Which shape fits?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                level.options.forEach { opt ->
                    val state = when {
                        picked == null -> OptionState.Idle
                        sameSpec(opt, level.answer) && sameSpec(opt, picked!!) -> OptionState.Correct
                        sameSpec(opt, picked!!) -> OptionState.Wrong
                        else -> OptionState.Idle
                    }
                    ShapeOption(
                        spec = opt,
                        state = state,
                        enabled = phase == Phase.Playing && picked == null,
                        onClick = {
                            picked = opt
                            if (sameSpec(opt, level.answer)) {
                                phase = Phase.Correct; onLevelPassed(levelIndex)
                            } else {
                                phase = Phase.Wrong; onMistake()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun sameSpec(a: ShapeSpec, b: ShapeSpec): Boolean =
    a.kind == b.kind && a.color == b.color && a.rotationDeg == b.rotationDeg

@Composable
private fun ShapeSequenceRow(sequence: List<ShapeSpec>, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        sequence.forEach { spec ->
            ShapeCell(spec, missing = false, accent = accent)
        }
        ShapeCell(spec = null, missing = true, accent = accent)
    }
}

@Composable
private fun ShapeCell(spec: ShapeSpec?, missing: Boolean, accent: Color) {
    val bg = if (missing) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        if (missing) {
            Text("?", color = accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        } else if (spec != null) {
            DrawShape(spec, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ShapeOption(
    spec: ShapeSpec,
    state: OptionState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when (state) {
        OptionState.Idle -> MaterialTheme.colorScheme.surface
        OptionState.Correct -> Color(0xFF2BD99F)
        OptionState.Wrong -> Color(0xFFFF6B6B)
    }
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .then(if (enabled) Modifier.clickableNoIndication(onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        DrawShape(spec, modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun DrawShape(spec: ShapeSpec, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.rotate(spec.rotationDeg)) {
        drawShape(spec.kind, spec.color, size)
    }
}

private fun DrawScope.drawShape(kind: ShapeKind, color: Color, size: Size) {
    val w = size.width
    val h = size.height
    when (kind) {
        ShapeKind.Circle -> drawCircle(color, radius = w / 2f, center = Offset(w / 2f, h / 2f))
        ShapeKind.Square -> drawRect(color, topLeft = Offset.Zero, size = size)
        ShapeKind.Triangle -> {
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, color)
        }
        ShapeKind.Diamond -> {
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }
            drawPath(path, color)
        }
    }
}
