package com.stusyncteam.stusync.api.modeus.auth

import okhttp3.OkHttpClient

class ModeusSession(
    val httpClient: OkHttpClient,
    val token: String
)