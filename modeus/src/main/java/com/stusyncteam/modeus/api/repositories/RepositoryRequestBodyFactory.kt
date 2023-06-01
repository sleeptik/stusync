package com.stusyncteam.modeus.api.repositories

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.stusyncteam.modeus.api.models.ModeusPerson
import com.stusyncteam.modeus.api.util.DateTimeUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal class RepositoryRequestBodyFactory {
    companion object {
        fun createSearchPersonRequestBody(fullName: String): RequestBody {
            val jsonObject = JsonObject()

            jsonObject.addProperty("fullName", fullName)
            jsonObject.addProperty("size", 25)
            jsonObject.addProperty("page", 0)
            jsonObject.addProperty("sort", "+fullName")

            val mediaType = "application/json".toMediaType()
            return jsonObject.toString().toRequestBody(mediaType)
        }

        fun createGetEventsRequestBody(person: ModeusPerson): RequestBody {
            val start = DateTimeUtils.getStartOfTheWeek()
            val end = DateTimeUtils.getEndOfTheWeek()

            val jsonObject = JsonObject().apply {
                val personId = JsonArray()
                personId.add(person.id.toString())

                add("attendeePersonId", personId)
                addProperty("size", 500)
                addProperty("timeMin", DateTimeUtils.dateTimeToString(start))
                addProperty("timeMax", DateTimeUtils.dateTimeToString(end))
            }

            val mediaType = "application/json".toMediaType()
            return jsonObject.toString().toRequestBody(mediaType)
        }

        fun createSelfPersonRequestBody(): RequestBody {
            val jsonObject = JsonObject()
            val mediaType = "application/json".toMediaType()
            return jsonObject.toString().toRequestBody(mediaType)
        }
    }
}