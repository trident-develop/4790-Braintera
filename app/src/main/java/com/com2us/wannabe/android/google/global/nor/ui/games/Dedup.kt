package com.com2us.wannabe.android.google.global.nor.ui.games

/**
 * Tries up to [maxAttempts] different seeds and returns the first generated
 * puzzle whose [fingerprintOf] key is not yet in [seen]. Adds the chosen
 * fingerprint to [seen] so the next call excludes it.
 *
 * If every attempt collides (after lots of play in the same session) we just
 * return the last one — better to show a slight repeat than to freeze.
 */
internal fun <T> generateUniquePuzzle(
    baseSeed: Int,
    seen: MutableSet<String>,
    maxAttempts: Int = 40,
    generator: (seed: Int) -> T,
    fingerprintOf: (T) -> String
): T {
    var seed = baseSeed
    var candidate = generator(seed)
    var attempts = 0
    while (fingerprintOf(candidate) in seen && attempts < maxAttempts) {
        seed += 17
        candidate = generator(seed)
        attempts += 1
    }
    seen += fingerprintOf(candidate)
    return candidate
}
