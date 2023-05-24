package com.stusyncteam.modeus.api.auth

import com.stusyncteam.modeus.api.util.QueryUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal class AuthRequestBodyFactory {
    companion object {
        fun createAdfsAuthRequestBody(userAuthData: UserCredentials): RequestBody {
            val formData = mapOf(
                "UserName" to userAuthData.username,
                "Password" to userAuthData.password,
                "AuthMethod" to "FormsAuthentication"
            )
            val queryString = QueryUtils.transformMapToQueryString(formData)
            return queryString.toRequestBody("application/x-www-form-urlencoded".toMediaType())
        }

        fun createCommonAuthRequestBody(commonAuthFormInputs: Map<String, String>): RequestBody {
            val queryString = QueryUtils.transformMapToQueryString(commonAuthFormInputs)
            return queryString.toRequestBody("application/x-www-form-urlencoded".toMediaType())
        }
    }
}