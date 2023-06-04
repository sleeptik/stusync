package com.stusyncteam.stusync.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("appPreferences")

abstract class Storage<T>(private val context: Context) where T : Any {
    companion object {
        private val gson = Gson()
    }

    suspend fun save(data: T) {
        val json = gson.toJson(data)
        context.dataStore.edit { it[getPreferencesKey()] = json }
    }

    suspend fun load(): T {
        val json = context.dataStore.data.map { it[getPreferencesKey()] }.firstOrNull()

        if (json != null) {
            return gson.fromJson(json, getJavaClass())
        }

        return getDefaultInstance()
    }

    protected abstract fun getPreferencesKey(): Preferences.Key<String>
    protected abstract fun getJavaClass(): Class<T>
    protected abstract fun getDefaultInstance(): T
}