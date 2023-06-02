package com.stusyncteam.stusync.api.google

import android.content.Context
import android.view.Display.Mode
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarRequest
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.stusyncteam.modeus.api.models.ModeusEvent
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

    fun prepareRequests(
        modeusEvents: MutableList<ModeusEvent>,
        googleEvents: MutableList<Event>
    ): Collection<CalendarRequest<*>> {
        val requests = modeusEvents.map {
            val event = GoogleEventBuilder(it)
                .setDefaultSummary()
                .setDefaultDescription()
                .setDefaultReminders()
                .setDefaultDates()
                .event

            calendar.events().insert("primary", event)

        }

        return requests.map { it as CalendarRequest<*> }
    }

    suspend fun getGoogleEvents(): MutableList<Event> {
        lateinit var events: List<Event>
        withContext(Dispatchers.IO) {
            events = calendar.events().list("primary").execute().items.filter {
                !it.description.isNullOrEmpty() && it.description.contains(
                    "ID="
                )
            }
        }
        return events.toMutableList()
    }

    suspend fun editAndDeleteEvents(
        modeusEvents: MutableList<ModeusEvent>,
        googleEvents: MutableList<Event>
    ): Pair<Int, Int> {
        var countEdit: Int = 0
        var countDelete: Int = googleEvents.size
        var have: Boolean = false;
        withContext(Dispatchers.IO) {
            if (googleEvents != null)
                for (it in googleEvents) {
                    have = false
                    lateinit var event: Event
                    for (modeusEvent in modeusEvents) {
                        if (it.description.contains("${modeusEvent.id}")) {
                            event = GoogleEventBuilder(modeusEvent)
                                .setDefaultSummary()
                                .setDefaultDescription()
                                .setDefaultReminders()
                                .setDefaultDates()
                                .event
                            calendar.events().update("primary", it.id, event).execute()

                            modeusEvents.remove(modeusEvent)
                            countEdit += 1
                            have = true
                            break
                        }
                    }
                    if (have) continue
                    calendar.events().delete("primary", it.id).execute()

                }
        }
        countDelete -= countEdit
        return Pair(countEdit, countDelete)
    }

    suspend fun executeAll(requests: Collection<CalendarRequest<*>>) {
        withContext(Dispatchers.IO) {
            requests.forEach { it.execute() }
        }
    }
}
