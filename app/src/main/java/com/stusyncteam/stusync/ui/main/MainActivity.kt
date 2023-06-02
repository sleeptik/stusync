@file:Suppress("JoinDeclarationAndAssignment")

package com.stusyncteam.stusync.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.modeus.ModeusSession
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import com.stusyncteam.stusync.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var consentLauncher: ActivityResultLauncher<Intent>
    private var openSettingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var tvCreated: MaterialTextView
    private lateinit var tvEdited: MaterialTextView
    private lateinit var tvDeleted: MaterialTextView

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
        tvCreated = findViewById(R.id.tv_last_sync_created)
        tvEdited = findViewById(R.id.tv_last_sync_modified)
        tvDeleted = findViewById(R.id.tv_last_sync_deleted)

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
            lateinit var pairCounts: Pair<Int,Int>
            var countCreate : Int = 0
            it.isEnabled = false
            lifecycleScope.launch(handleRequestExecutionWithAuth())
            {
                withContext(Dispatchers.IO) {
                    val googleCalendar = GoogleCalendarFacade.fromContext(this@MainActivity)

                    val self = modeusSession.getMyself()
                    val events = modeusSession.getPersonEvents(self).toMutableList()

                    val googleEvents = googleCalendar.getGoogleEvents()
                    pairCounts = googleCalendar.editAndDeleteEvents(events,googleEvents)
                    val requests = googleCalendar.prepareRequests(events,googleEvents)
                    countCreate = requests.size
                    googleCalendar.executeAll(requests)
                }
                it.isEnabled = true
                tvCreated.text = "Created: ${countCreate}"
                tvEdited.text = "Modified: ${pairCounts.first}"
                tvDeleted.text = "Deleted: ${pairCounts.second}"
            }
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
