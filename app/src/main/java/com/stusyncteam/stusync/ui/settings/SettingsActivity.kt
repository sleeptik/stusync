package com.stusyncteam.stusync.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.databinding.ActivitySettingsBinding
import com.stusyncteam.stusync.storage.settings.SettingsDataStore
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {
    private val settingsViewModel = SettingsViewModel()
    private val settingsDataStore = SettingsDataStore(this)

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            settingsViewModel.apply {
                importSettings.value = settingsDataStore.loadImportSettings()
                notificationSettings.value = settingsDataStore.loadNotificationsSettings()
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
                settingsDataStore.saveImportSettings(importSettings.value!!)
                settingsDataStore.saveNotificationSettings(notificationSettings.value!!)
            }
        }
    }
}