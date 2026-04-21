package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2us.wannabe.android.google.global.nor.data.GameStats
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.data.Prefs
import com.com2us.wannabe.android.google.global.nor.ui.common.PlayfulBackground

enum class MainTab(val label: String, val emoji: String) {
    Games("Games", "\uD83C\uDFAE"),        // game controller
    Play("Play", "\u25B6"),                // play
    Stats("Stats", "\uD83D\uDCCA")         // bar chart
}

/**
 * Root screen for MainActivity: paints the animated background, swaps the
 * three tabs, and renders a custom floating bottom nav.
 */
@Composable
fun MainShell(prefs: Prefs) {
    var tab by remember { mutableIntStateOf(0) } // 0 Games, 1 Play, 2 Stats
    var queuedGame by remember { mutableStateOf<GameType?>(null) }
    var statsTick by remember { mutableIntStateOf(0) }

    // Re-read stats each time a level completes or tab switches.
    val allStats = remember(statsTick, tab) { prefs.allStats() }
    val statsMap = remember(allStats) { allStats.associateBy { it.type } }

    val activeGame = remember(tab, queuedGame, statsTick) {
        when {
            tab == MainTab.Play.ordinal && queuedGame != null -> queuedGame!!
            tab == MainTab.Play.ordinal -> prefs.lastPlayedGame() ?: GameType.all.first()
            else -> queuedGame ?: prefs.lastPlayedGame() ?: GameType.all.first()
        }
    }

    // Once Play has been opened with a queued game, clear the queue so a later
    // bottom-nav tap defaults back to "last played".
    LaunchedEffect(tab, queuedGame) {
        if (tab != MainTab.Play.ordinal) queuedGame = null
    }

    PlayfulBackground {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = tab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "tab"
            ) { selected ->
                val contentPadding = PaddingValues(bottom = 96.dp)
                when (selected) {
                    MainTab.Games.ordinal -> GamesScreen(
                        stats = statsMap,
                        onStartGame = { game ->
                            queuedGame = game
                            tab = MainTab.Play.ordinal
                        },
                        contentPadding = contentPadding
                    )
                    MainTab.Play.ordinal -> PlayScreen(
                        activeGame = activeGame,
                        prefs = prefs,
                        onStatsChanged = { statsTick++ },
                        contentPadding = contentPadding
                    )
                    else -> StatsScreen(
                        stats = allStats,
                        contentPadding = contentPadding
                    )
                }
            }

            BottomNav(
                current = tab,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                onSelected = { tab = it }
            )
        }
    }
}

@Composable
private fun BottomNav(
    current: Int,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(28.dp), clip = false, spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.values().forEachIndexed { index, t ->
            NavItem(
                tab = t,
                selected = current == index,
                onClick = { onSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavItem(
    tab: MainTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer
            )
        )
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickableNoIndicationLocal(onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tab.emoji, fontSize = 18.sp)
            Text(
                tab.label,
                color = fg,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/** Local alias so this file stays independent from the games package. */
@Composable
private fun Modifier.clickableNoIndicationLocal(onClick: () -> Unit): Modifier {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

