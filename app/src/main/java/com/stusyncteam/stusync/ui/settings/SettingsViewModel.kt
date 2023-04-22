package com.stusyncteam.stusync.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stusyncteam.stusync.ui.settings.data.ImportSettings
import com.stusyncteam.stusync.ui.settings.data.NotificationSettings

class SettingsViewModel : ViewModel() {
    val importSettings: MutableLiveData<ImportSettings> by lazy { MutableLiveData<ImportSettings>() }
    val notificationSettings: MutableLiveData<NotificationSettings> by lazy { MutableLiveData<NotificationSettings>() }
}
