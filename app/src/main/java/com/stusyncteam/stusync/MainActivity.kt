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
import com.stusyncteam.stusync.api.google.GoogleSignInFacade


class MainActivity : AppCompatActivity() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var account: GoogleSignInAccount? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            else
                Log.e("GoogleLogin", "Bad result code")

            findViewById<TextView>(R.id.plain_text).text = account?.email ?: "null"
        }

        val googleSignInButton = findViewById<SignInButton>(R.id.sign_in_button)
        googleSignInButton.setOnClickListener {
            val signInFacade = GoogleSignInFacade(this)
            account = signInFacade.getSignedInAccount()

            if (account != null)
                return@setOnClickListener

            launcher.launch(signInFacade.getSignInIntent())
        }
    }
}
