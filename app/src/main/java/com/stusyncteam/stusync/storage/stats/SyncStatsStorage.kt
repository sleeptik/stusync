package com.stusyncteam.stusync.storage.stats

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stusyncteam.stusync.storage.Storage

class SyncStatsStorage(context: Context) : Storage<SyncStats>(context) {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("sync_stats")
    }

    override fun getJavaClass(): Class<SyncStats> {
        return SyncStats::class.java
    }
}