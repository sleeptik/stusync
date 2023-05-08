package com.stusyncteam.stusync.api.modeus.auth

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ModeusRequestBodyFactory {
    companion object {
        fun createAdfsAuthRequestBody(userAuthData: ModeusLoginUserData): RequestBody {
            val formData = mapOf(
                "UserName" to userAuthData.username,
                "Password" to userAuthData.password,
                "AuthMethod" to "FormsAuthentication",
            )
            val queryString = QueryUtil.transformMapToQueryString(formData)
            return queryString.toRequestBody("application/x-www-form-urlencoded".toMediaType())
        }

        fun createCommonAuthRequestBody(commonAuthFormInputs: Map<String, String>): RequestBody {
            val queryString = QueryUtil.transformMapToQueryString(commonAuthFormInputs)
            return queryString.toRequestBody("application/x-www-form-urlencoded".toMediaType())
        }
    }
}