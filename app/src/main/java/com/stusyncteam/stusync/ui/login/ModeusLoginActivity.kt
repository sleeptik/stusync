package com.stusyncteam.stusync.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.modeus.api.auth.UserCredentials
import com.stusyncteam.stusync.R
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import com.stusyncteam.stusync.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ModeusLoginActivity : AppCompatActivity(), OnLoginFinished {
    private val credentialsStorage: CredentialsStorage by lazy { CredentialsStorage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modeus_login)

        runBlocking {
            val result = kotlin.runCatching { loadCredentials() }

            if (result.isSuccess)
                onLoginFinished()
        }

        findViewById<Button>(R.id.btn_sign_in_modeus).setOnClickListener {
            lifecycleScope.launch {
                it.isEnabled = false

                val login = findViewById<EditText>(R.id.et_login).text.toString()
                val password = findViewById<EditText>(R.id.et_password).text.toString()

                if (login == "" || password == "")
                    return@launch

                val userCredentials = UserCredentials(login, password)

                withContext(Dispatchers.IO) {
                    try {
                        ModeusSignIn.login(userCredentials)
                        saveCredentials(userCredentials)
                        onLoginFinished()
                    } catch (e: Exception) {
                        Log.e("login error", e.message.toString())
                        it.isEnabled = true
                    }
                }
            }
        }
    }

    private suspend fun saveCredentials(userCredentials: UserCredentials) {
        credentialsStorage.save(userCredentials)
    }

    private suspend fun loadCredentials(): UserCredentials {
        return credentialsStorage.load()
    }

    override fun onLoginFinished() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }
}