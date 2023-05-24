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
    }

    fun getPersonByName(
        httpClient: OkHttpClient,
        authToken: String,
        fullName: String
    ): ModeusPerson {
        val body = RepositoryRequestBodyFactory.createSearchPersonRequestBody(fullName)

        val request: Request = Request.Builder()
            .url(ApiUrl)
            .post(body)
            .addHeader("authorization", "Bearer $authToken")
            .build()

        httpClient.newCall(request).execute().use {
            return getPersonFromResponse(it)
        }
    }

    private fun getPersonFromResponse(response: Response): ModeusPerson {
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