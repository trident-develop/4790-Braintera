package com.com2us.wannabe.android.google.global.nor.data.rain.mydata

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLEncoder

fun addBack(intent: Intent, map: Map<String, String>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val trId = intent.getStringExtra("trackingId")
            if (trId != null) {
                val fcmToken = withContext(Dispatchers.IO) {
                    FirebaseMessaging.getInstance().token.await()
                }
                val url = "https://${map["pos"]}"
                val client = OkHttpClient()

                val fullUrl = "$url?" + "${map["track"]}=$trId" + "&${map["newBack"]}=${URLEncoder.encode(fcmToken, "UTF-8")}"
                val request = Request.Builder().url(fullUrl).build()

                client.newCall(request).enqueue(
                    object : okhttp3.Callback {
                        override fun onFailure(call: Call, e: IOException) {}

                        override fun onResponse(call: Call, response: Response) {
                            response.close()
                        }
                    }
                )
            }
        } catch (_: Exception) {
        }
    }
}
