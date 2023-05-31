package com.stusyncteam.stusync.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.stusyncteam.stusync.R

class GoogleLoginActivity : AppCompatActivity(), ILoginActivity {
    private val signInGoogleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK || it.data == null)
            return@registerForActivityResult

        try {
            GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            onLoginFinished()
        } catch (e: ApiException) {
            Log.wtf("LoginActivityGoogleLogin", "Unexpected error parsing sign-in result")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_login)

        if (GoogleSignIn.getLastSignedInAccount(this) != null)
            onLoginFinished()

        findViewById<SignInButton>(R.id.btn_sign_in_google).setOnClickListener {
            val signInOptions = GoogleSignInOptions.Builder().requestEmail().build()
            val signInClient = GoogleSignIn.getClient(this, signInOptions)
            val signInIntent = signInClient.signInIntent
            signInGoogleLauncher.launch(signInIntent)
        }
    }

    override fun onLoginFinished() {
        val modeusLoginActivityIntent = Intent(this, ModeusLoginActivity::class.java)
        startActivity(modeusLoginActivityIntent)
    }
}