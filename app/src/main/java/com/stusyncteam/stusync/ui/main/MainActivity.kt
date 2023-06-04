package com.stusyncteam.stusync.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.modeus.ModeusSession
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.background.AutoSyncWorkScheduler
import com.stusyncteam.stusync.databinding.ActivityMainBinding
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import com.stusyncteam.stusync.storage.settings.AutoSyncSettingsStorage
import com.stusyncteam.stusync.storage.stats.SyncStatsStorage
import com.stusyncteam.stusync.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val syncStatsViewModel = SyncStatsViewModel()

    private val openSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val modeusSession by lazy { runBlocking { getNewModeusSession() } }

    private val askConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
    }
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is UserRecoverableAuthIOException) {
            askConsentLauncher.launch(throwable.intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).also {
            lifecycleScope.launch {
                refreshSyncStats()
                it.stats = syncStatsViewModel
            }
            it.lifecycleOwner = this
        }

        findViewById<ImageButton>(R.id.btn_open_settings).also {
            it.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                openSettingLauncher.launch(intent)
            }
        }

        findViewById<Button>(R.id.btn_manual_sync).also {
            it.setOnClickListener {
                lifecycleScope.launch(coroutineExceptionHandler) {
                    it.isEnabled = false

                    withContext(Dispatchers.IO) {
                        val person = modeusSession.getMyself()
                        val events = modeusSession.getPersonEvents(person)

                        val calendar = GoogleCalendarFacade.fromContext(this@MainActivity)
                        calendar.updateCalendar(events)
                    }

                    refreshSyncStats()

                    it.isEnabled = true
                }
            }
        }

        findViewById<SwitchCompat>(R.id.sw_auto_sync).also {
            lifecycleScope.launch { it.isChecked = getAutoSyncState() }
            it.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    when (isChecked) {
                        true -> AutoSyncWorkScheduler.scheduleAutoSync(this@MainActivity)
                        false -> AutoSyncWorkScheduler.disableAutoSync(this@MainActivity)
                    }
                    setAutoSyncState(isChecked)
                }
            }
        }
    }

    private suspend fun getNewModeusSession(): ModeusSession = withContext(Dispatchers.IO) {
        val credentials = CredentialsStorage(this@MainActivity).load()
        return@withContext ModeusSignIn.login(credentials)
    }

    private suspend fun refreshSyncStats() {
        syncStatsViewModel.syncStats.value = SyncStatsStorage(this).load()
    }

    private suspend fun getAutoSyncState(): Boolean {
        val settings = AutoSyncSettingsStorage(this).load()
        return settings.isEnabled
    }

    private suspend fun setAutoSyncState(isEnabled: Boolean) {
        val storage = AutoSyncSettingsStorage(this)
        val settings = storage.load()
        settings.isEnabled = isEnabled
        storage.save(settings)
    }
}
