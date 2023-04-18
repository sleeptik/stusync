package com.stusyncteam.stusync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.stusyncteam.stusync.api.google.CalendarFacadeFactory
import com.stusyncteam.stusync.api.google.GoogleSignInFacade
import com.stusyncteam.stusync.api.modeus.models.MockCollections
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var account: GoogleSignInAccount? = null

    private var signInLauncher: ActivityResultLauncher<Intent>
    private var consentLauncher: ActivityResultLauncher<Intent>

    init {
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
                } catch (e: ApiException) {
                    Toast.makeText(this, "Couldn't get logged account", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        consentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != RESULT_OK) {
                Toast.makeText(this, "Consent was not given", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            val signInFacade = GoogleSignInFacade(this)
            account = signInFacade.getLastSignedInAccount()

            if (account == null)
                signInLauncher.launch(signInFacade.getSignInIntent())

            findViewById<TextView>(R.id.plain_text).text = account?.email ?: "null"
        }

        findViewById<Button>(R.id.test_button).setOnClickListener {
            val calendar = CalendarFacadeFactory.createCalendarFacade(this)

            val requests = calendar.prepareRequests(MockCollections.createLessons())

            lifecycleScope.launch(handleRequestExecutionWithAuth()) {
                calendar.executeAll(requests)
            }
        }
    }

    private fun handleRequestExecutionWithAuth(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { coroutineContext, throwable ->
            if (throwable is UserRecoverableAuthIOException) {
                consentLauncher.launch(throwable.intent)
            }
        }
    }
}
