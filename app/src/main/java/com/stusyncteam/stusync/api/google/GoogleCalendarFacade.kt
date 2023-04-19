package com.stusyncteam.stusync.api.google

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarRequest
import com.google.api.services.calendar.CalendarScopes
import com.stusyncteam.stusync.api.modeus.models.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections


class GoogleCalendarFacade private constructor(private val calendar: Calendar) {
    companion object {
        private const val applicationName = "stusync"

        fun fromContext(context: Context): GoogleCalendarFacade {
            val scopes = Collections.singleton(CalendarScopes.CALENDAR)
            val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
            credential.selectedAccount = credential.googleAccountManager.accounts.first()

            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val calendar = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build()

            return GoogleCalendarFacade(calendar)
        }
    }

    fun prepareRequests(lessons: List<Lesson>): Collection<CalendarRequest<*>> {
        val requests = lessons.map {
            val event = LessonEventBuilder(it)
                .setDefaultSummary()
                .setDefaultDescription()
                .setDefaultReminders()
                .setDefaultDates()
                .event

            calendar.events().insert("primary", event)
        }

        return requests.map { it as CalendarRequest<*> }
    }

    suspend fun executeAll(requests: Collection<CalendarRequest<*>>) {
        withContext(Dispatchers.IO) {
            requests.forEach { it.execute() }
        }
    }
}
