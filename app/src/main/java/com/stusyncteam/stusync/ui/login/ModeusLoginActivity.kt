package com.stusyncteam.stusync.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.modeus.api.auth.UserCredentials
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ModeusLoginActivity : AppCompatActivity(), ILoginActivity {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modeus_login)

        findViewById<Button>(R.id.btn_sign_in_modeus).setOnClickListener {
            val loginEditText = findViewById<EditText>(R.id.et_login)
            val passwordEditText = findViewById<EditText>(R.id.et_password)

            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (login == "" || password == "")
                return@setOnClickListener

            val userCredentials = UserCredentials(login, password)

            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        ModeusSignIn.login(userCredentials)
                    }
                }
                onLoginFinished()
            } catch (_: Exception) {

            }
        }
    }

    override fun onLoginFinished() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }
}