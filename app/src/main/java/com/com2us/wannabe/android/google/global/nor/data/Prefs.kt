package com.com2us.wannabe.android.google.global.nor.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Immutable

/**
 * Small snapshot of per-game stats used by the Stats screen.
 */
@Immutable
data class GameStats(
    val type: GameType,
    val levelReached: Int, // next level to play (0-based). If == totalLevels, the game is completed.
    val wins: Int,
    val mistakes: Int,
    val lastPlayedAt: Long
) {
    val totalLevels: Int get() = type.totalLevels
    val completedLevels: Int get() = levelReached.coerceAtMost(totalLevels)
    val completionPercent: Int get() =
        if (totalLevels == 0) 0 else (completedLevels * 100f / totalLevels).toInt()
    val isCompleted: Boolean get() = completedLevels >= totalLevels
}

/**
 * Central persistence helper. Everything is stored in a single SharedPreferences
 * file ("braintera_prefs") so progress, stats and the "last played" pointer stay
 * together and survive app restarts.
 */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ----- Last played game ---------------------------------------------------

    fun lastPlayedGame(): GameType? = GameType.fromId(sp.getString(KEY_LAST_PLAYED, null))

    fun setLastPlayedGame(game: GameType) {
        sp.edit().putString(KEY_LAST_PLAYED, game.id).apply()
    }

    // ----- Per-game progress --------------------------------------------------

    /** The next level to play for [game] (0-based). Clamped to totalLevels. */
    fun levelReached(game: GameType): Int {
        val raw = sp.getInt(key(game, KEY_LEVEL), 0)
        return raw.coerceIn(0, game.totalLevels)
    }

    fun setLevelReached(game: GameType, level: Int) {
        sp.edit().putInt(key(game, KEY_LEVEL), level.coerceIn(0, game.totalLevels)).apply()
    }

    /** Marks [level] as cleared; advances the saved pointer only if it is ahead. */
    fun markLevelCompleted(game: GameType, level: Int) {
        val next = (level + 1).coerceAtMost(game.totalLevels)
        val current = levelReached(game)
        sp.edit().apply {
            if (next > current) putInt(key(game, KEY_LEVEL), next)
            putInt(key(game, KEY_WINS), wins(game) + 1)
            putLong(key(game, KEY_LAST_AT), System.currentTimeMillis())
            apply()
        }
    }

    fun registerMistake(game: GameType) {
        sp.edit()
            .putInt(key(game, KEY_MISTAKES), mistakes(game) + 1)
            .putLong(key(game, KEY_LAST_AT), System.currentTimeMillis())
            .apply()
    }

    /** Reset a single game's progress (used by the "Replay" flow). */
    fun resetGame(game: GameType) {
        sp.edit()
            .putInt(key(game, KEY_LEVEL), 0)
            .apply()
    }

    private fun wins(game: GameType): Int = sp.getInt(key(game, KEY_WINS), 0)
    private fun mistakes(game: GameType): Int = sp.getInt(key(game, KEY_MISTAKES), 0)
    private fun lastAt(game: GameType): Long = sp.getLong(key(game, KEY_LAST_AT), 0L)

    fun statsFor(game: GameType): GameStats = GameStats(
        type = game,
        levelReached = levelReached(game),
        wins = wins(game),
        mistakes = mistakes(game),
        lastPlayedAt = lastAt(game)
    )

    fun allStats(): List<GameStats> = GameType.all.map(::statsFor)

    // ----- Keys ---------------------------------------------------------------

    private fun key(game: GameType, suffix: String) = "game_${game.id}_$suffix"

    companion object {
        private const val FILE = "braintera_prefs"
        private const val KEY_LAST_PLAYED = "last_played"
        private const val KEY_LEVEL = "level"
        private const val KEY_WINS = "wins"
        private const val KEY_MISTAKES = "mistakes"
        private const val KEY_LAST_AT = "last_at"
    }
}
