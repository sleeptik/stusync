package com.stusyncteam.modeus.api.auth

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class AuthCookieJar : CookieJar {
    private val jar = mutableListOf<Cookie>()

    private val cookiesToCollect = listOf(
        "tc01",
        "session-cookie",
        "MSISAuth",
        "JSESSIONID",
        "commonAuthId"
    )

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return when (url.host) {
            "utmn.modeus.org" -> {
                jar.filter { it.name == "tc01" }
            }

            "fs.utmn.ru" -> {
                jar.filter {
                    it.name == "session-cookie"
                            || it.name == "MSISAuth"
                }
            }

            "auth.modeus.org" -> {
                jar.filter {
                    it.name == "tc01"
                            || it.name == "JSESSIONID"
                            || it.name == "commmonAuthId"
                }
            }

            else -> {
                emptyList()
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val filteredResponseCookies = cookies.filter { cookiesToCollect.contains(it.name) }
        jar.addAll(filteredResponseCookies)
    }
}