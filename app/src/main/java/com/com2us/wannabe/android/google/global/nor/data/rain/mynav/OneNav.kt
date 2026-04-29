package com.com2us.wannabe.android.google.global.nor.data.rain.mynav

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object OneNav {
    private val _screen = MutableStateFlow<NavPoint>(NavPoint.Welcome)
    val screen: StateFlow<NavPoint> = _screen

    fun point(screen: NavPoint) {
        _screen.value = screen
    }
}

sealed class NavPoint {
    data object Welcome : NavPoint()
    data object InternetProblem : NavPoint()
    data object Game : NavPoint()
    data object MenuPoint : NavPoint()
    data object Settings : NavPoint()
}
