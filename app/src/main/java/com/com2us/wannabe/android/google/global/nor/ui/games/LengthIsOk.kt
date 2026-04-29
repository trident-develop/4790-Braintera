package com.com2us.wannabe.android.google.global.nor.ui.games

import android.content.Context
import com.com2us.wannabe.android.google.global.nor.data.rain.mydata.StateOfStorage
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.NavPoint
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.OneNav.point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LengthIsOk(data: String?, context: Context, onClose: () -> Unit) {
    init {
        val hand = LengthManager()
        data?.let { d ->
            when {
                hand.short == d.take(hand.short.length) -> {
                    point(NavPoint.MenuPoint)
                }

                d.length < 3 -> {
                    throw IllegalStateException()
                }

                hand.short != d.take(hand.short.length) -> {
                    onClose()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            StateOfStorage.safeSave(context, d)
                        } catch (_: Exception) {
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

val pnau = "braintera.space/dqayf"
val pnagk = "g5uzl"
val pnaftk = "r00w8hgc"
