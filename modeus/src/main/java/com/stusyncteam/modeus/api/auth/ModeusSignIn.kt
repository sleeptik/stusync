package com.stusyncteam.modeus.api.auth

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.stusyncteam.modeus.ModeusSession
import com.stusyncteam.modeus.api.util.QueryUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URL

class ModeusSignIn private constructor(private val userCredentials: UserCredentials) {
    companion object {
        fun login(userCredentials: UserCredentials): ModeusSession {
            val signIn = ModeusSignIn(userCredentials)

            val loginUrl = let {
                val appConfig = signIn.getAppConfigJson()
                signIn.createLoginUrl(appConfig)
            }

            val adfsAuthUrl = signIn.getAdfsAuthUrl(loginUrl)
            val adfsAuthResult = signIn.authorizeThroughAdfs(adfsAuthUrl)

            val authorizedSessionUrl = signIn.authorizeThroughCommonAuth(adfsAuthResult)

            val authorizedToken = signIn.getAuthorizedToken(authorizedSessionUrl)

            return ModeusSession(signIn.httpClient, authorizedToken)
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .cookieJar(AuthCookieJar())
        .build()

    private fun getAppConfigJson(): JsonObject {
        val request = Request.Builder()
            .url("https://utmn.modeus.org/assets/app.config.json")
            .get()
            .build()
        val response = httpClient.newCall(request).execute()
        return response.use { JsonParser.parseString(it.body?.string()).asJsonObject }
    }

    private fun createLoginUrl(appConfig: JsonElement): URL {
        val wso = appConfig.asJsonObject["wso"].asJsonObject
        val loginUrl = URL(wso["loginUrl"].asString)
        val clientId = wso["clientId"].asString

        val authorizationData = mapOf(
            "client_id" to clientId,
            "redirect_uri" to "https://utmn.modeus.org/",
            "response_type" to "id_token",
            "scope" to "openid",
            "nonce" to SecretGenerator.createSecretString(),
            "state" to SecretGenerator.createSecretString()
        )

        val queryString = QueryUtils.transformMapToQueryString(authorizationData)
        return QueryUtils.joinUrlToQueryString(loginUrl, queryString)
    }

    private fun getAdfsAuthUrl(loginUrl: URL): URL {
        val adfsGetUrl = let {
            val request = Request.Builder().get().url(loginUrl).build()
            val response = httpClient.newCall(request).execute()
            response.use { URL(it.priorResponse?.header("location")) }
        }

        val adfsPostUrl = let {
            val request = Request.Builder().get().url(adfsGetUrl).build()
            val response = httpClient.newCall(request).execute()
            response.use { r ->
                val htmlDocument = Jsoup.parse(r.body?.string())
                val form = htmlDocument.forms().first { it.id() == "loginForm" }
                val postAction = form.attr("action")
                URL("https://fs.utmn.ru${postAction}")
            }
        }

        return adfsPostUrl
    }

    private fun authorizeThroughAdfs(adfsAuthUrl: URL): Map<String, String> {
        val request = Request.Builder()
            .post(AuthRequestBodyFactory.createAdfsAuthRequestBody(userCredentials))
            .url(adfsAuthUrl)
            .build()

        val response = httpClient.newCall(request).execute()
        return response.use { r ->
            val htmlDocument = Jsoup.parse(r.body?.string())
            val errorElement = htmlDocument.getElementById("errorText")

            if (errorElement != null && errorElement.text() != "")
                throw Exception() //TODO EXCEPTION

            val form = htmlDocument.forms().first { it.attr("name") == "hiddenform" }
            form.getElementsByTag("input")
                .filter { element -> element.attr("type") == "hidden" }
                .associate { it.attr("name") to it.`val`() }
        }
    }

    private fun authorizeThroughCommonAuth(adfsAuthResult: Map<String, String>): URL {
        val commonAuthRequestBody =
            AuthRequestBodyFactory.createCommonAuthRequestBody(adfsAuthResult)

        val request = Request.Builder()
            .url("https://auth.modeus.org/commonauth")
            .post(commonAuthRequestBody)
            .build()
        val response = httpClient.newCall(request).execute()
        return URL(response.use { it.priorResponse?.header("location") })
    }

    private fun getAuthorizedToken(authorizedSessionUrl: URL): String {
        val request = Request.Builder()
            .url(authorizedSessionUrl)
            .head()
            .build()
        val response = httpClient.newCall(request).execute()
        val modeusUrl = response.use { it.request.url }


        return modeusUrl.fragment!!
            .split("&")
            .first { it.startsWith("id_token") }
            .split("=")
            .first { !it.startsWith("id_token") }
    }
}