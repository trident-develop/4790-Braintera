package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameStats
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.EmojiBadge
import com.com2us.wannabe.android.google.global.nor.ui.common.PlayfulCard
import com.com2us.wannabe.android.google.global.nor.ui.common.SectionTitle
import com.com2us.wannabe.android.google.global.nor.ui.common.SlimProgress
import com.com2us.wannabe.android.google.global.nor.ui.common.accentGradient
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainBlue
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainCoral
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainGreen
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainPurple
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    stats: List<GameStats>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val totalCompleted = stats.sumOf { it.completedLevels }
    val grandTotal = stats.sumOf { it.totalLevels }
    val totalWins = stats.sumOf { it.wins }
    val totalMistakes = stats.sumOf { it.mistakes }
    val completedGames = stats.count { it.isCompleted }
    val favorite = stats.maxByOrNull { it.wins }?.takeIf { it.wins > 0 }?.type
    val overallProgress = if (grandTotal == 0) 0f else totalCompleted / grandTotal.toFloat()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 48.dp + contentPadding.calculateTopPadding(),
            bottom = 24.dp + contentPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionTitle(
                title = "Your Progress",
                subtitle = "Daily training keeps the brain lively."
            )
        }
        item {
            OverallCard(
                progress = overallProgress,
                totalCompleted = totalCompleted,
                grandTotal = grandTotal,
                completedGames = completedGames,
                totalWins = totalWins,
                totalMistakes = totalMistakes,
                favorite = favorite
            )
        }
        items(stats, key = { it.type.id }) { stat ->
            PerGameCard(stat)
        }
    }
}

@Composable
private fun OverallCard(
    progress: Float,
    totalCompleted: Int,
    grandTotal: Int,
    completedGames: Int,
    totalWins: Int,
    totalMistakes: Int,
    favorite: GameType?
) {
    val gradient = Brush.linearGradient(
        colors = listOf(BrainBlue, BrainPurple, BrainCoral)
    )
    PlayfulCard {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = progress,
                    size = 92,
                    stroke = 12,
                    gradient = gradient
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Overall progress",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "$totalCompleted of $grandTotal levels",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "$completedGames of 5 games completed",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryTile(label = "Wins", value = totalWins.toString(), color = BrainGreen, modifier = Modifier.weight(1f))
                SummaryTile(label = "Mistakes", value = totalMistakes.toString(), color = BrainCoral, modifier = Modifier.weight(1f))
                SummaryTile(label = "Accuracy", value = accuracyString(totalWins, totalMistakes), color = BrainYellow, modifier = Modifier.weight(1f))
            }
            if (favorite != null) {
                Spacer(Modifier.height(12.dp))
                FavoriteRow(favorite)
            }
        }
    }
}

private fun accuracyString(wins: Int, mistakes: Int): String {
    val total = wins + mistakes
    if (total == 0) return "—"
    return "${(wins * 100f / total).toInt()}%"
}

@Composable
private fun FavoriteRow(game: GameType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(game.accent.copy(alpha = 0.10f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmojiBadge(text = game.emoji, background = accentGradient(game.accent, game.secondary), size = 40)
        Column(Modifier.weight(1f)) {
            Text(
                "Most played",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                game.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Text("\u2B50", fontSize = 22.sp)
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            color = color,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun PerGameCard(stat: GameStats) {
    val game = stat.type
    val gradient = accentGradient(game.accent, game.secondary)

    PlayfulCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(text = game.emoji, background = gradient, size = 48)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        game.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        statusLine(stat),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "${stat.completionPercent}%",
                    color = game.accent,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.height(10.dp))
            SlimProgress(
                progress = stat.completedLevels / stat.totalLevels.toFloat(),
                color = game.accent
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat("Levels", "${stat.completedLevels}/${stat.totalLevels}", game.accent, Modifier.weight(1f))
                MiniStat("Wins", stat.wins.toString(), BrainGreen, Modifier.weight(1f))
                MiniStat("Mistakes", stat.mistakes.toString(), BrainCoral, Modifier.weight(1f))
            }
        }
    }
}

private fun statusLine(stat: GameStats): String {
    if (stat.isCompleted) return "Completed • keep replaying for fun"
    if (stat.wins == 0 && stat.mistakes == 0) return "Not started yet"
    val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
    val last = if (stat.lastPlayedAt > 0) " • last ${fmt.format(Date(stat.lastPlayedAt))}" else ""
    return "On level ${stat.completedLevels + 1}$last"
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    size: Int,
    stroke: Int,
    gradient: Brush,
    content: @Composable () -> Unit
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = stroke.dp.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            drawArc(
                color = Color(0x22000000),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx)
            )
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        content()
    }
}
