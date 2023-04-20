package com.stusyncteam.stusync.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.api.google.GoogleSignInFacade
import com.stusyncteam.stusync.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private var googleAccount: GoogleSignInAccount? = null
    // TODO: private val modeusAccount: blablaClass? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            try {
                googleAccount = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            } catch (e: ApiException) {
                Log.wtf("LoginActivityGoogleLogin", "Unexpected error parsing sign-in result");
            }
        }

        startMainActivityIfLoggedIn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.google_sign_in_button).setOnClickListener {
            askUserToLogInGoogleAccount()
        }

        googleAccount = GoogleSignIn.getLastSignedInAccount(this)

        startMainActivityIfLoggedIn()
    }

    private fun askUserToLogInGoogleAccount() {
        val signInFacade = GoogleSignInFacade(this)

        if (googleAccount == null) {
            googleSignInLauncher.launch(signInFacade.getSignInIntent())
        }
    }

    private fun startMainActivityIfLoggedIn() {
        if (googleAccount == null) {
            Toast.makeText(this, "Google login account failed", Toast.LENGTH_LONG)
                .show()
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
    }
}