package com.stusyncteam.stusync.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.api.modeus.ical.ICalCalendarFacade
import com.stusyncteam.stusync.api.modeus.models.Lesson
import com.stusyncteam.stusync.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import net.fortuna.ical4j.data.ParserException


class MainActivity : AppCompatActivity() {
    private var consentLauncher: ActivityResultLauncher<Intent>
    private var openCalendarAndUploadLauncher: ActivityResultLauncher<Array<String>>
    private var goToSettingsLauncher: ActivityResultLauncher<Intent>

    init {
        consentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != RESULT_OK) {
                Toast.makeText(this, "Consent was not given", Toast.LENGTH_LONG)
                    .show()
            }
        }

        openCalendarAndUploadLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            if (it == null)
                return@registerForActivityResult

            var lessons: List<Lesson>

            contentResolver.openInputStream(it)!!.use { stream ->
                try {
                    lessons = ICalCalendarFacade.fromStream(stream).lessons
                } catch (e: ParserException) {
                    Log.e("ICal", e.message.toString())
                    return@registerForActivityResult
                }
            }

            val calendar = GoogleCalendarFacade.fromContext(this)
            val requests = calendar.prepareRequests(lessons)

            lifecycleScope.launch(handleRequestExecutionWithAuth()) {
                calendar.executeAll(requests)
            }
        }

        goToSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.test_button).setOnClickListener {
            openCalendarAndUploadLauncher.launch(arrayOf("text/calendar"))
        }

        findViewById<FloatingActionButton>(R.id.go_to_settings).setOnClickListener {
            goToSettingsLauncher.launch(Intent(this, SettingsActivity::class.java))
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
