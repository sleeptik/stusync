package com.stusyncteam.stusync.api.google

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleSignInFacade(private val context: Context) {
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getSignInIntent(): Intent {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, googleSignInOptions).signInIntent
    }
}

