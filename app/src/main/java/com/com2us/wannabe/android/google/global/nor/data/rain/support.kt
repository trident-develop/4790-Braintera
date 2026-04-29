package com.com2us.wannabe.android.google.global.nor.data.rain

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.com2us.wannabe.android.google.global.nor.data.rain.InitWorker
import com.com2us.wannabe.android.google.global.nor.data.rain.collectA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

suspend fun support(
    raw: String,
    id: String,
    context: Context,
    initWorker: InitWorker
): String {
    val encoded = URLEncoder.encode(raw, Charsets.UTF_8.name())

    val params = buildMap {
        put(initWorker.map["gk"], runAfter(context))
        put(initWorker.map["rk"], encoded)
        put(initWorker.map["ei"], id)
        put(initWorker.map["paramOne"], getTime(context))
        put(initWorker.map["clock"], collectA(context))
        put(initWorker.map["weeks"], getDev())
    }

    return params
        .filterValues { !it.isNullOrBlank() }
        .entries
        .joinToString("&") { (key, value) -> "$key=$value" }
}

fun getTime(context: Context): String {
    val packageInfo: PackageInfo =
        context.packageManager.getPackageInfo(context.packageName, 0)
    val time = packageInfo.firstInstallTime.toString()
    return time
}

fun getDev(): String {
    val device =
        Build.BRAND.replaceFirstChar { it.titlecase(Locale.getDefault()) } + " " + Build.MODEL
    val encoded2 = URLEncoder.encode(device, StandardCharsets.UTF_8.toString())
    return encoded2
}

private suspend fun runAfter(context: Context): String = withContext(Dispatchers.IO) {
    try {
        val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
        if (!info.isLimitAdTrackingEnabled) {
            info.id ?: "00000000-0000-0000-0000-000000000000"
        } else {
            "00000000-0000-0000-0000-000000000000"
        }
    } catch (_: Exception) {
        "00000000-0000-0000-0000-000000000000"
    }
}