package com.com2us.wannabe.android.google.global.nor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.com2us.wannabe.android.google.global.nor.data.GameType
import com.com2us.wannabe.android.google.global.nor.data.Prefs
import com.com2us.wannabe.android.google.global.nor.ui.games.MemoryGridGame
import com.com2us.wannabe.android.google.global.nor.ui.games.NumberChainGame
import com.com2us.wannabe.android.google.global.nor.ui.games.OddOneOutGame
import com.com2us.wannabe.android.google.global.nor.ui.games.PatternMatchGame
import com.com2us.wannabe.android.google.global.nor.ui.games.ShapeLogicGame

/**
 * Hosts the currently playing game. Whenever [activeGame] changes we:
 *  - mark it as the "last played" game in prefs,
 *  - feed the game its saved [startLevel] (where the user left off),
 *  - persist completed levels and mistakes back to prefs.
 */
@Composable
fun PlayScreen(
    activeGame: GameType,
    prefs: Prefs,
    onStatsChanged: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    // Record every entry into a game as "last played" so the Play tab remembers.
    LaunchedEffect(activeGame) {
        prefs.setLastPlayedGame(activeGame)
    }

    val startLevel = prefs.levelReached(activeGame)

    AnimatedContent(
        targetState = activeGame,
        modifier = modifier.fillMaxSize().padding(contentPadding).padding(top = 48.dp),
        transitionSpec = {
            (fadeIn(tween(250)) togetherWith fadeOut(tween(200)))
        },
        label = "game-switch"
    ) { game ->
        val onPassed = { level: Int ->
            prefs.markLevelCompleted(game, level)
            onStatsChanged()
        }
        val onMistake = {
            prefs.registerMistake(game)
            onStatsChanged()
        }
        when (game) {
            GameType.PATTERN_MATCH -> PatternMatchGame(game, startLevel, onPassed, onMistake)
            GameType.NUMBER_CHAIN -> NumberChainGame(game, startLevel, onPassed, onMistake)
            GameType.SHAPE_LOGIC -> ShapeLogicGame(game, startLevel, onPassed, onMistake)
            GameType.MEMORY_GRID -> MemoryGridGame(game, startLevel, onPassed, onMistake)
            GameType.ODD_ONE_OUT -> OddOneOutGame(game, startLevel, onPassed, onMistake)
        }
    }
}
