package com.stusyncteam.modeus.api.repositories

import com.google.gson.JsonParser
import com.stusyncteam.modeus.api.models.ModeusPerson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.UUID

internal class PersonRepository {
    companion object {
        private const val ApiUrl =
            "https://utmn.modeus.org/schedule-calendar-v2/api/people/persons/search/"
        private const val MySelfApiUrl =
            "https://utmn.modeus.org/students-app/api/pages/student-card/my/primary"
    }

    fun getPersonByName(
        httpClient: OkHttpClient,
        authToken: String,
        fullName: String
    ): ModeusPerson {
        val body = RepositoryRequestBodyFactory.createSearchPersonRequestBody(fullName)

        val request = Request.Builder()
            .url(ApiUrl)
            .post(body)
            .addHeader("authorization", "Bearer $authToken")
            .build()

        httpClient.newCall(request).execute().use {
            return getPersonFromSearchResponse(it)
        }
    }

    fun getMyself(
        httpClient: OkHttpClient,
        authToken: String
    ): ModeusPerson {
        val body = RepositoryRequestBodyFactory.createSelfPersonRequestBody()

        val request = Request.Builder()
            .url(MySelfApiUrl)
            .post(body)
            .addHeader("authorization", "Bearer $authToken")
            .build()
        httpClient.newCall(request).execute().use {
            return getPersonFromSelfResponse(it)
        }
    }

    private fun getPersonFromSelfResponse(response: Response): ModeusPerson {
        val jsonString = response.body?.string()
        val json = JsonParser.parseString(jsonString).asJsonObject

        return ModeusPerson(
            UUID.fromString(json["personId"].asString),
            json["name"].asString,
            json["surname"].asString,
            json["middleName"].asString
        )
    }

    private fun getPersonFromSearchResponse(response: Response): ModeusPerson {
        val jsonResponse = JsonParser.parseString(response.body!!.string()).asJsonObject
        val embedded = jsonResponse["_embedded"].asJsonObject

        val persons = embedded["persons"].asJsonArray
        val person = persons.first().asJsonObject

        return ModeusPerson(
            UUID.fromString(person["id"].asString),
            person["firstName"].asString,
            person["lastName"].asString,
            person["middleName"].asString
        )
    }
}