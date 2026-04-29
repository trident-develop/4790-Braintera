package com.com2us.wannabe.android.google.global.nor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.com2us.wannabe.android.google.global.nor.ui.screens.LoadingScreen
import com.com2us.wannabe.android.google.global.nor.ui.screens.NoInternetScreen
import com.com2us.wannabe.android.google.global.nor.ui.screens.Transaction
import com.com2us.wannabe.android.google.global.nor.ui.theme.BrainteraTheme
import com.com2us.wannabe.android.google.global.nor.data.rain.InitWorker
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.NavPoint
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.OneNav

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
        val initWorker = InitWorker(this, intent)
        setContent {
            val navController: NavHostController = rememberNavController()
            NavHost(
                navController = navController, startDestination = "LoadingScreen"
            ) {
                composable("LoadingScreen") {
                    val screen by OneNav.screen.collectAsState()

                    when (screen) {
                        NavPoint.Welcome -> LoadingScreen(this@LoadingActivity, initWorker)
                        NavPoint.MenuPoint -> Transaction {
                            startActivity(
                                Intent(this@LoadingActivity, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }
                        NavPoint.InternetProblem -> NoInternetScreen()
                        else -> {}
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
