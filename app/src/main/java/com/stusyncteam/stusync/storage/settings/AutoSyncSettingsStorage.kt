package com.stusyncteam.stusync.storage.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stusyncteam.stusync.storage.Storage

class AutoSyncSettingsStorage(context: Context) : Storage<AutoSyncSettings>(context) {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("auto_sync_settings")
    }

    override fun getJavaClass(): Class<AutoSyncSettings> {
        return AutoSyncSettings::class.java
    }
}