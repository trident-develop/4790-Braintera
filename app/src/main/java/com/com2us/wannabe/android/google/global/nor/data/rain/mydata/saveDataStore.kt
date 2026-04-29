package com.com2us.wannabe.android.google.global.nor.data.rain.mydata

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.com2us.wannabe.android.google.global.nor.ui.games.saveNewOne
import kotlinx.coroutines.flow.first

private val Context.saveDataStore by preferencesDataStore(name = "nmfd")

object StateOfStorage {
    private val KEY_STRING = stringPreferencesKey("sdfg")
    private val KEY_LEVEL = stringPreferencesKey("level")
    private val KEY_POINTS = stringPreferencesKey("coins")

    suspend fun safeSave(context: Context, value: String) {
        val dataStore = context.saveDataStore

        val existing = dataStore.data.first()[KEY_STRING]
        if (existing != null) {
            throw IllegalStateException("Value already saved: $existing")
        }

        saveNewOne()
        dataStore.edit { prefs ->
            prefs[KEY_STRING] = value
        }
    }

    suspend fun getUrl(context: Context): String {
        val dataStore = context.saveDataStore
        return dataStore.data.first()[KEY_STRING] ?: ""
    }

    suspend fun getLevel(context: Context): String {
        val dataStore = context.saveDataStore
        return dataStore.data.first()[KEY_LEVEL] ?: "Level: 1"
    }

    suspend fun getPoints(context: Context): String {
        val dataStore = context.saveDataStore
        return dataStore.data.first()[KEY_POINTS] ?: "Points: 0"
    }
}
