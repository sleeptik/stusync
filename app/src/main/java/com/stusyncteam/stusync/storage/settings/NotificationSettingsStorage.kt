package com.stusyncteam.stusync.storage.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stusyncteam.stusync.storage.Storage

class NotificationSettingsStorage(context: Context) : Storage<NotificationSettings>(context) {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("notification_settings")
    }

    override fun getJavaClass(): Class<NotificationSettings> {
        return NotificationSettings::class.java
    }
}