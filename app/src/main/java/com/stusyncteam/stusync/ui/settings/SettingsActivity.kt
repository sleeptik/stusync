package com.stusyncteam.stusync.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.databinding.ActivitySettingsBinding
import com.stusyncteam.stusync.storage.settings.ImportSettingsStorage
import com.stusyncteam.stusync.storage.settings.NotificationSettingsStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {
    private val settingsViewModel = SettingsViewModel()
    private val importSettingsStorage = ImportSettingsStorage(this)
    private val notificationSettingsStorage = NotificationSettingsStorage(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
            .also {
                lifecycleScope.launch {
                    settingsViewModel.apply {
                        importSettings.value = importSettingsStorage.load()
                        notificationSettings.value = notificationSettingsStorage.load()
                    }
                    it.settings = settingsViewModel
                }
                it.lifecycleOwner = this
            }
    }

    override fun onPause() {
        super.onPause()

        runBlocking {
            settingsViewModel.apply {
                importSettingsStorage.save(importSettings.value!!)
                notificationSettingsStorage.save(notificationSettings.value!!)
            }
        }
    }
}