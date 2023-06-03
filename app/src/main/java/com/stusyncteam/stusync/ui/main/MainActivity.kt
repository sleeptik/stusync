@file:Suppress("JoinDeclarationAndAssignment")

package com.stusyncteam.stusync.ui.main

import android.Manifest.permission.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.modeus.ModeusSession
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.background.BackgroundSyncWorker
import com.stusyncteam.stusync.databinding.ActivitySettingsBinding
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import com.stusyncteam.stusync.ui.settings.SettingsActivity
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var consentLauncher: ActivityResultLauncher<Intent>
    private var openSettingsLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivitySettingsBinding
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
            requestContactPermissionForSync()
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



        val autoSyncSwitch = findViewById<CompoundButton>(R.id.sw_auto_sync)
        autoSyncSwitch.isChecked = true
        autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            val workName = "stusync_auto_sync"
            val workManager = WorkManager.getInstance(this)

            if (isChecked) {
                val backgroundSyncRequest = BackgroundSyncWorker.createPeriodicWorkRequest(15)
                workManager.enqueueUniquePeriodicWork(
                    workName,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    backgroundSyncRequest
                )
            } else {
                workManager.cancelUniqueWork(workName)
            }
        }
    }


    private fun requestContactPermissionForSync() {
        if (!EasyPermissions.hasPermissions(this, GET_ACCOUNTS)) {
            val permissionRequest = PermissionRequest.Builder(this)
                .perms(arrayOf(GET_ACCOUNTS))
                .build()
            EasyPermissions.requestPermissions(this, permissionRequest)
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
