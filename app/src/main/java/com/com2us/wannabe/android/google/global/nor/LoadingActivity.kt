package com.com2us.wannabe.android.google.global.nor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.com2us.wannabe.android.google.global.nor.ui.screens.LoadingScreen
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainteraTheme
import kotlinx.coroutines.delay

/**
 * Initial splash. Shows the loading animation for exactly 2 seconds, then
 * hands off to MainActivity. Declared as the LAUNCHER activity in the manifest.
 */
class LoadingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            BrainteraTheme {
                LoadingScreen()
                LaunchedEffect(Unit) {
                    delay(LOADING_DURATION_MS)
                    startActivity(
                        Intent(this@LoadingActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    // Finish so back press from Main does not return to the splash.
                    finish()
                    // Fade transition — new API on 34+, fallback on older devices.
                    @Suppress("DEPRECATION")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(
                            OVERRIDE_TRANSITION_OPEN,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    companion object {
        private const val LOADING_DURATION_MS = 2000L
    }
}
