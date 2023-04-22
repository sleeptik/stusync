package com.stusyncteam.stusync.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.stusyncteam.stusync.ui.settings.data.ImportSettings
import com.stusyncteam.stusync.ui.settings.data.NotificationSettings
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        private val importSettingsKey = stringPreferencesKey("import_settings")
        private val notificationsSettingsKey = stringPreferencesKey("notifications_settings")

        private val gson = Gson()
    }

    suspend fun saveNotificationSettings(notificationSettings: NotificationSettings) {
        val json = gson.toJson(notificationSettings)
        context.dataStore.edit { it[notificationsSettingsKey] = json }
    }

    suspend fun loadNotificationsSettings(): NotificationSettings {
        val json = context.dataStore.data.map { it[notificationsSettingsKey] }.firstOrNull()

        if (json != null) {
            return gson.fromJson(json, NotificationSettings::class.java)
        }

        return NotificationSettings()
    }

    suspend fun saveImportSettings(importSettings: ImportSettings) {
        val json = gson.toJson(importSettings)
        context.dataStore.edit { it[importSettingsKey] = json }
    }

    suspend fun loadImportSettings(): ImportSettings {
        val json = context.dataStore.data.map { it[importSettingsKey] }.firstOrNull()

        if (json != null) {
            return gson.fromJson(json, ImportSettings::class.java)
        }

        return ImportSettings()
    }
}