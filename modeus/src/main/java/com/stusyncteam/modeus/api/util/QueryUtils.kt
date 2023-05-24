package com.stusyncteam.modeus.api.util

import java.net.URL
import java.net.URLEncoder

internal class QueryUtils {
    companion object {
        fun transformMapToQueryString(map: Map<String, String>): String {
            return map
                .map {
                    val encodedKey = URLEncoder.encode(it.key, Charsets.UTF_8.name())
                    val encodedValue = URLEncoder.encode(it.value, Charsets.UTF_8.name())
                    "${encodedKey}=${encodedValue}"
                }
                .joinToString("&")
        }

        fun joinUrlToQueryString(url: URL, query: String): URL {
            return URL("${url}?${query}")
        }
    }
}