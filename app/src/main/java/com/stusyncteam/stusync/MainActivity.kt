package com.stusyncteam.stusync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.stusyncteam.stusync.api.google.GoogleSignInFacade


class MainActivity : AppCompatActivity() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var account: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
                } catch (e: ApiException) {
                    Log.e("GoogleSignIn", "Couldn't sign in: ${e.message.toString()}")
                }
            }
        }

        val googleSignInButton = findViewById<SignInButton>(R.id.sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInFacade = GoogleSignInFacade(this)
            account = signInFacade.getLastSignedInAccount()

            if (account == null)
                launcher.launch(signInFacade.getSignInIntent())

            findViewById<TextView>(R.id.plain_text).text = account?.email ?: "null"
        }
    }
}
