package com.stusyncteam.modeus.api.repositories

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.stusyncteam.modeus.api.models.ModeusEvent
import com.stusyncteam.modeus.api.models.ModeusPerson
import com.stusyncteam.modeus.api.util.DateTimeUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.UUID

internal class EventRepository {
    companion object {
        private const val ApiUrl =
            "https://utmn.modeus.org/schedule-calendar-v2/api/calendar/events/search/"
    }

    fun getPersonEvents(
        httpClient: OkHttpClient,
        authToken: String,
        person: ModeusPerson
    ): List<ModeusEvent> {
        val body = RepositoryRequestBodyFactory.createGetEventsRequestBody(person)

        val request: Request = Request.Builder()
            .url(ApiUrl)
            .post(body)
            .addHeader("authorization", "Bearer $authToken")
            .build()

        httpClient.newCall(request).execute().use {
            return getEventsFromResponse(it)
        }
    }

    private fun getEventsFromResponse(response: Response): List<ModeusEvent> {
        val jsonResponse = JsonParser.parseString(response.body!!.string()).asJsonObject

        val eventsJson = jsonResponse["_embedded"].asJsonObject["events"].asJsonArray
        val coursesJson =
            jsonResponse["_embedded"].asJsonObject["course-unit-realizations"].asJsonArray

        val events = eventsJson
            .map { element -> element.asJsonObject }
            .map {
                ModeusEvent(
                    UUID.fromString(it["id"].asString),
                    getEventCourseName(it, coursesJson),
                    "Building not set",
                    "Classroom not set",
                    DateTimeUtils.stringToDate(it["start"].asString),
                    DateTimeUtils.stringToDate(it["end"].asString),
                )
            }

        return events
    }

    private fun getEventCourseName(event: JsonObject, courses: JsonArray): String {
        val eventLinks = event["_links"].asJsonObject
        val courseId =
            eventLinks["course-unit-realization"].asJsonObject["href"].asString.substring(1)
        val associatedCourse = courses.first { it.asJsonObject["id"].asString == courseId }
        return associatedCourse.asJsonObject["nameShort"].asString
    }
}


