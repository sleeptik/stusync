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

        val embedded = jsonResponse["_embedded"].asJsonObject

        val eventsJson = embedded["events"].asJsonArray
        val coursesJson = embedded["course-unit-realizations"].asJsonArray
        val eventAttendeesJson = embedded["event-attendees"].asJsonArray
        val personsJson = embedded["persons"].asJsonArray
        val cycleRealizationsJson = embedded["cycle-realizations"].asJsonArray
        val eventLocations = embedded["event-locations"].asJsonArray
        val eventRooms = embedded["event-rooms"].asJsonArray
        val rooms = embedded["rooms"].asJsonArray


        val events = eventsJson
            .map { element -> element.asJsonObject }
            .map {
                ModeusEvent(
                    UUID.fromString(it["id"].asString),
                    getEventCourseName(it, coursesJson),
                    getBuilding(it,eventLocations,eventRooms,rooms),
                    getClassroom(it,eventLocations,eventRooms,rooms),
                    getEventLessonType(it, cycleRealizationsJson),
                    DateTimeUtils.stringToDate(it["start"].asString),
                    DateTimeUtils.stringToDate(it["end"].asString),
                    getEventTeachers(it, eventAttendeesJson, personsJson),
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

    private fun getEventTeachers(
        event: JsonObject,
        eventAttendees: JsonArray,
        persons: JsonArray
    ): List<ModeusPerson> {
        val currentEventAttendees = eventAttendees
            .filter {
                val links = it.asJsonObject["_links"].asJsonObject
                val eventId = links["event"].asJsonObject["href"].asString.substring(1)
                eventId == event["id"].asString
            }

        val eventTeacherIds = currentEventAttendees
            .map {
                val links = it.asJsonObject["_links"].asJsonObject
                links["person"].asJsonObject["href"].asString.substring(1)
            }

        val teachers = persons.filter { eventTeacherIds.contains(it.asJsonObject["id"].asString) }

        return teachers
            .map { it.asJsonObject }
            .map {
                ModeusPerson(
                    UUID.fromString(it["id"].asString),
                    it["firstName"].asString,
                    it["lastName"].asString,
                    it["middleName"].asString
                )
            }
    }

    private fun getEventLessonType(event: JsonObject, cycleRealizations: JsonArray): String {
        val eventLinks = event["_links"].asJsonObject
        val lessonTypeId = eventLinks["cycle-realization"].asJsonObject["href"]
            .asString.substring(1)
        val lessonType = cycleRealizations.first { it.asJsonObject["id"].asString == lessonTypeId }

        return lessonType.asJsonObject["name"].asString
    }

    private fun getClassroom(event: JsonObject,eventLocations: JsonArray,eventRooms:JsonArray,rooms:JsonArray): String {
        val eventLocationId= event["id"].asString
        val customLocation=eventLocations.first{it.asJsonObject["eventId"].asString==eventLocationId }.asJsonObject["customLocation"]
        if (!customLocation.isJsonNull)
            return " "
        else {
            val eventRoomId = eventLocations.first{it.asJsonObject["eventId"].asString==eventLocationId }.asJsonObject["_links"].asJsonObject["event-rooms"].asJsonObject["href"].asString.substring(1)
            val roomId = eventRooms.first{it.asJsonObject["id"].asString==eventRoomId}.asJsonObject["_links"].asJsonObject["room"].asJsonObject["href"].asString.substring(1)
            val room = rooms.first{it.asJsonObject["id"].asString==roomId}.asJsonObject
            return "Аудитория ".plus(room["name"].asString.substringBefore("("))
        }
    }

    private fun getBuilding(event: JsonObject,eventLocations: JsonArray,eventRooms:JsonArray,rooms:JsonArray): String {
        val eventLocationId= event["id"].asString
        val customLocation=eventLocations.first{it.asJsonObject["eventId"].asString==eventLocationId }.asJsonObject["customLocation"]
        if (!customLocation.isJsonNull)
            return customLocation.asString
        else {
            val eventRoomId = eventLocations.first{it.asJsonObject["eventId"].asString==eventLocationId }.asJsonObject["_links"].asJsonObject["event-rooms"].asJsonObject["href"].asString.substring(1)
            val roomId = eventRooms.first{it.asJsonObject["id"].asString==eventRoomId}.asJsonObject["_links"].asJsonObject["room"].asJsonObject["href"].asString.substring(1)
            val building = rooms.first{it.asJsonObject["id"].asString==roomId}.asJsonObject["building"].asJsonObject
            return building["name"].asString.plus(" - ").plus(building["address"].asString)
        }
    }
}


