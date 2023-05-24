package com.stusyncteam.modeus.api.auth

import java.security.SecureRandom

internal class SecretGenerator {
    companion object {
        fun createSecretString(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}