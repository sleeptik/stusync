@file:Suppress("JoinDeclarationAndAssignment")

package com.stusyncteam.stusync.ui.main

import android.Manifest.permission.*
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.modeus.ModeusSession
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.background.AutoSyncWorkScheduler
import com.stusyncteam.stusync.databinding.ActivitySettingsBinding
import com.stusyncteam.stusync.permissions.PermissionRequester
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import com.stusyncteam.stusync.storage.settings.AutoSyncSettings
import com.stusyncteam.stusync.storage.settings.AutoSyncSettingsStorage
import com.stusyncteam.stusync.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private var consentLauncher: ActivityResultLauncher<Intent>
    private var openSettingsLauncher: ActivityResultLauncher<Intent>

    private val autoSyncSettingsStorage = AutoSyncSettingsStorage(this)
    private val syncViewModel = SyncStatsViewModel()

    private lateinit var modeusSession: ModeusSession

    init {
        consentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != RESULT_OK) {
                Toast.makeText(this, "Consent was not given", Toast.LENGTH_LONG)
                    .show()
            }
        }

        openSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking {
            withContext(Dispatchers.IO) {
                val credentials = CredentialsStorage(this@MainActivity).load()
                modeusSession = ModeusSignIn.login(credentials!!)
            }
        }

        findViewById<FloatingActionButton>(R.id.btn_open_settings).setOnClickListener {
            openSettingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_manual_sync).setOnClickListener {
            PermissionRequester.requestGetAccountsPermission(this)

            it.isEnabled = false
            lifecycleScope.launch(handleRequestExecutionWithAuth())
            {
                withContext(Dispatchers.IO) {
                    val googleCalendar = GoogleCalendarFacade.fromContext(this@MainActivity)

                    val self = modeusSession.getMyself()
                    val events = modeusSession.getPersonEvents(self).toMutableList()

                    googleCalendar.updateCalendar(events)
                }
                it.isEnabled = true
            }
        }

        val autoSyncSwitch = findViewById<SwitchCompat>(R.id.sw_auto_sync)
        autoSyncSwitch.isChecked = loadAutoSyncSettings().isEnabled
        autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AutoSyncWorkScheduler.scheduleAutoSync(this)
            } else {
                AutoSyncWorkScheduler.disableAutoSync(this)
            }

            val autoSyncSettings = loadAutoSyncSettings().also { it.isEnabled = isChecked }
            saveAutoSyncSettings(autoSyncSettings)
        }
    }

    private fun saveAutoSyncSettings(autoSyncSettings: AutoSyncSettings) {
        runBlocking {
            autoSyncSettingsStorage.save(autoSyncSettings)
        }
    }

    private fun loadAutoSyncSettings(): AutoSyncSettings {
        return runBlocking {
            autoSyncSettingsStorage.load() ?: AutoSyncSettings()
        }
    }

    private fun handleRequestExecutionWithAuth(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            if (throwable is UserRecoverableAuthIOException) {
                consentLauncher.launch(throwable.intent)
            }
        }
    }
}
