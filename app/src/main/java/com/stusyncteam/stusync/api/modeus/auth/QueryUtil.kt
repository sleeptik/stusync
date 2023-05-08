package com.stusyncteam.stusync.api.modeus.auth

import java.net.URL
import java.net.URLEncoder

class QueryUtil {
    companion object {
        fun transformMapToQueryString(map: Map<String, String>): String {
            return map
                .map {
                    val encodedKey = URLEncoder.encode(it.key, "utf-8")
                    val encodedValue = URLEncoder.encode(it.value, "utf-8")
                    "${encodedKey}=${encodedValue}"
                }
                .joinToString("&")
        }

        fun joinUrlToQueryString(url: URL, query: String): URL {
            return URL("${url}?${query}")
        }
    }
}