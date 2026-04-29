package com.com2us.wannabe.android.google.global.nor.data.rain

import android.content.Intent
import androidx.activity.ComponentActivity
import com.com2us.wannabe.android.google.global.nor.data.rain.mydata.addBack
import com.com2us.wannabe.android.google.global.nor.data.rain.mydata.sortData
import com.com2us.wannabe.android.google.global.nor.ui.common.ClientCustom
import com.com2us.wannabe.android.google.global.nor.ui.common.ViewCustom

class InitWorker(activity: ComponentActivity, intent: Intent) {
    var newView: ViewCustom = ViewCustom(activity, ClientCustom(activity))
    val map by lazy { sortData() }

    init {
        addBack(intent, map)
    }

    fun newV(): ViewCustom {
        return newView
    }
}
