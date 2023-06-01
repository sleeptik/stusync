package com.stusyncteam.stusync.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.databinding.ActivitySettingsBinding
import com.stusyncteam.stusync.storage.settings.ImportSettings
import com.stusyncteam.stusync.storage.settings.ImportSettingsStorage
import com.stusyncteam.stusync.storage.settings.NotificationSettings
import com.stusyncteam.stusync.storage.settings.NotificationSettingsStorage
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {
    private val settingsViewModel = SettingsViewModel()
    private val importSettingsStorage = ImportSettingsStorage(this)
    private val notificationSettingsStorage = NotificationSettingsStorage(this)

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            settingsViewModel.apply {
                importSettings.value = importSettingsStorage.load() ?: ImportSettings()
                notificationSettings.value = notificationSettingsStorage.load() ?: NotificationSettings()
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.lifecycleOwner = this

        binding.settings = settingsViewModel
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