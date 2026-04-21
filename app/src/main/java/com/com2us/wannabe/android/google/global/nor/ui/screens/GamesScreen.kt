package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameStats
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.ui.common.EmojiBadge
import com.com2us.wannabe.android.google.global.nor.ui.common.GradientButton
import com.com2us.wannabe.android.google.global.nor.ui.common.InfoChip
import com.com2us.wannabe.android.google.global.nor.ui.common.PlayfulCard
import com.com2us.wannabe.android.google.global.nor.ui.common.SectionTitle
import com.com2us.wannabe.android.google.global.nor.ui.common.SlimProgress
import com.com2us.wannabe.android.google.global.nor.ui.common.accentGradient

@Composable
fun GamesScreen(
    stats: Map<GameType, GameStats>,
    onStartGame: (GameType) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    var expanded by remember { mutableStateOf<GameType?>(null) }

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
                title = "Brain Games",
                subtitle = "Pick a puzzle and keep your mind sharp."
            )
            Spacer(Modifier.height(6.dp))
        }
        items(GameType.all, key = { it.id }) { game ->
            val stat = stats[game]
            GameCard(
                game = game,
                stats = stat,
                isExpanded = expanded == game,
                onToggle = { expanded = if (expanded == game) null else game },
                onStart = {
                    expanded = null
                    onStartGame(game)
                }
            )
        }
    }
}

@Composable
private fun GameCard(
    game: GameType,
    stats: GameStats?,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onStart: () -> Unit
) {
    val gradient = accentGradient(game.accent, game.secondary)
    val completed = stats?.completedLevels ?: 0
    val total = game.totalLevels
    val percent = stats?.completionPercent ?: 0
    val isFinished = stats?.isCompleted == true

    PlayfulCard(onClick = onToggle) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(text = game.emoji, background = gradient, size = 56)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = game.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                InfoChip(
                    label = if (isFinished) "Done" else "$completed/$total",
                    color = game.accent
                )
            }
            Spacer(Modifier.height(12.dp))
            SlimProgress(
                progress = if (total == 0) 0f else completed / total.toFloat(),
                color = game.accent
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(Modifier.padding(top = 14.dp)) {
                    ExpandedSection(
                        description = game.description,
                        goal = game.goal,
                        percentLabel = "$percent% complete",
                        accent = game.accent
                    )
                    Spacer(Modifier.height(14.dp))
                    GradientButton(
                        text = if (isFinished) "Play again" else if (completed > 0) "Continue • Level ${completed + 1}" else "Start Playing",
                        gradient = gradient,
                        onClick = onStart
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedSection(
    description: String,
    goal: String,
    percentLabel: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DotIcon(accent); Text(
                "How it works",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DotIcon(accent); Text(
                "Goal",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = goal,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = percentLabel,
            style = MaterialTheme.typography.labelMedium,
            color = accent
        )
    }
}

@Composable
private fun DotIcon(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
