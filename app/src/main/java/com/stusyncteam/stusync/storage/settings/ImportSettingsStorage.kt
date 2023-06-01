package com.stusyncteam.stusync.storage.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stusyncteam.stusync.storage.Storage

class ImportSettingsStorage(context: Context) : Storage<ImportSettings>(context) {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("import_settings")
    }

    override fun getJavaClass(): Class<ImportSettings> {
        return ImportSettings::class.java
    }
}