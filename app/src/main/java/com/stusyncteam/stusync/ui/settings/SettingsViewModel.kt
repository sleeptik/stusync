package com.stusyncteam.stusync.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stusyncteam.stusync.storage.settings.ImportSettings
import com.stusyncteam.stusync.storage.settings.NotificationSettings

class SettingsViewModel : ViewModel() {
    val importSettings: MutableLiveData<ImportSettings> by lazy { MutableLiveData<ImportSettings>() }
    val notificationSettings: MutableLiveData<NotificationSettings> by lazy { MutableLiveData<NotificationSettings>() }
}
