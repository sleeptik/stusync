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
        val eventAttendsJson=jsonResponse["_embedded"].asJsonObject["event-attendees"].asJsonArray
        val teachersJson=jsonResponse["_embedded"].asJsonObject["persons"].asJsonArray
        val cycleJson = jsonResponse["_embedded"].asJsonObject["cycle-realizations"].asJsonArray

        val events = eventsJson
            .map { element -> element.asJsonObject }
            .map {
                ModeusEvent(
                    UUID.fromString(it["id"].asString),
                    getEventCourseName(it, coursesJson),
                    "Building not set",
                    "Classroom not set",
                    getTeacherName(it,eventAttendsJson,teachersJson),
                    getLessonType(it,cycleJson),
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

    private fun getTeacherName(event: JsonObject,eventAttends: JsonArray, teachers: JsonArray): String{
        val eventId = event["id"].asString
        val eventAttend=eventAttends.filter{ it.asJsonObject["_links"].asJsonObject["event"].asJsonObject["href"].asString.substring(1)==eventId}
        val teacherId = eventAttend.map{it.asJsonObject["_links"].asJsonObject["person"].asJsonObject["href"].asString.substring(1)}
        val teacher = teachers.filter{teacherId.contains(it.asJsonObject["id"].asString )}

        return teacher.map{it.asJsonObject["fullName"].asString}.joinToString(postfix=".",separator =", ")
    }

    private fun getLessonType(event:JsonObject,cycle: JsonArray): String {
        val eventLinks = event["_links"].asJsonObject
        val cycleId =
            eventLinks["cycle-realization"].asJsonObject["href"].asString.substring(1)
        val cycleRealization= cycle.first{it.asJsonObject["id"].asString==cycleId}

        return cycleRealization.asJsonObject["name"].asString

    }
}


