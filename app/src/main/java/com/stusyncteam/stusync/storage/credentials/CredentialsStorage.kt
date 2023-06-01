package com.stusyncteam.stusync.storage.credentials

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stusyncteam.modeus.api.auth.UserCredentials
import com.stusyncteam.stusync.storage.Storage

class CredentialsStorage(context: Context) : Storage<UserCredentials>(context) {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("modeus_user_credentials")
    }

    override fun getJavaClass(): Class<UserCredentials> {
        return UserCredentials::class.java
    }
}