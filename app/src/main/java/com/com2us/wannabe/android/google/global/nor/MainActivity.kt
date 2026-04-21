package com.com2us.wannabe.android.google.global.nor

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.com2us.wannabe.android.google.global.nor.data.Prefs
import com.com2us.wannabe.android.google.global.nor.ui.screens.MainShell
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainteraTheme

/**
 * Host for the three-tab experience. All state (selected tab, queued game,
 * stats refresh) lives inside [MainShell] so the activity itself stays thin.
 */
class MainActivity : ComponentActivity() {

    private val windowController by lazy { WindowInsetsControllerCompat(window, window.decorView) }
    private var multiTouchDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Prefs(applicationContext)
        setContent {
            BrainteraTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainShell(prefs = prefs)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        windowController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) {
            if (!multiTouchDetected) {
                multiTouchDetected = true
                val cancelEvent = MotionEvent.obtain(ev)
                cancelEvent.action = MotionEvent.ACTION_CANCEL
                super.dispatchTouchEvent(cancelEvent)
                cancelEvent.recycle()
            }
            return true
        }

        if (multiTouchDetected) {
            if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
                multiTouchDetected = false
            }
            return true
        }

        return super.dispatchTouchEvent(ev)
    }
}
