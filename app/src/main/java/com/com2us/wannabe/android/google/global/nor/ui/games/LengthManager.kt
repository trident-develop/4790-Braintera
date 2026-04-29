package com.com2us.wannabe.android.google.global.nor.ui.games

class LengthManager {
    val scheme = "https"
    private val escape = "://."
    private val baseDomain = "brainteraspace"
    private val index = 9

    val dom: String by lazy {
        baseDomain.substring(0, index) +
                escape.last() +
                baseDomain.substring(index)
    }

    private val randomEscapeChar: Char by lazy {
        escape.dropLast(1).drop(1).random()
    }

    val short: String by lazy {
        "$scheme${escape.dropLast(1)}$dom$randomEscapeChar"
    }

    val long: String by lazy {
        "${short}mreozl$randomEscapeChar"
    }
}
