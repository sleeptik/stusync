package com.stusyncteam.stusync.api.modeus.auth

import java.security.SecureRandom

class ModeusSecretGenerator {
    companion object {
        fun createSecretString(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}