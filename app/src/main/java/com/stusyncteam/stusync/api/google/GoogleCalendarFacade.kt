package com.stusyncteam.stusync.api.google

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarRequest
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.stusyncteam.modeus.api.models.ModeusEvent
import com.stusyncteam.stusync.storage.stats.SyncStats
import com.stusyncteam.stusync.storage.stats.SyncStatsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.Date


class GoogleCalendarFacade private constructor(
    private val calendar: Calendar,
    private val context: Context
) {
    companion object {
        private const val APPLICATION_NAME = "stusync"
        private const val CALENDAR_ID = "primary"

        fun fromContext(context: Context): GoogleCalendarFacade {
            val scopes = Collections.singleton(CalendarScopes.CALENDAR)
            val credential = GoogleAccountCredential.usingOAuth2(context, scopes)

            val currentAccount = GoogleSignIn.getLastSignedInAccount(context)
            val deviceAccounts = credential.googleAccountManager.accounts
            credential.selectedAccount = deviceAccounts.first { it.name == currentAccount?.email }

            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val calendar = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()

            return GoogleCalendarFacade(calendar, context)
        }
    }

    private val syncStatsStorage = SyncStatsStorage(context)

    suspend fun updateCalendar(modeusEvents: List<ModeusEvent>) = withContext(Dispatchers.IO) {
        val googleEvents = getGoogleEvents()

        val updateRequests = getUpdateRequests(modeusEvents, googleEvents)
        val deleteRequests = getDeleteRequests(modeusEvents, googleEvents)
        val insertRequests = getInsertRequests(modeusEvents, googleEvents)

        val requests = updateRequests.plus(deleteRequests).plus(insertRequests)
        executeAll(requests)

        val syncStats = syncStatsStorage.load()

        syncStats.apply {
            lastSync = Date()
            totalModifiedEvents = updateRequests.size
            totalDeletedEvents = deleteRequests.size
            totalCreatedEvents = insertRequests.size
        }

        syncStatsStorage.save(syncStats)
    }

    private fun getGoogleEvents(): List<Event> {
        return calendar.events().list(CALENDAR_ID).execute().items
            .filter { isModeusEvent(it) }
            .toList()
    }

    private fun isModeusEvent(event: Event): Boolean {
        return !event.description.isNullOrEmpty()
                && event.description.contains(GoogleEventFactory.MODEUS_UUID_PREFIX)
    }

    private fun getUpdateRequests(
        modeusEvents: List<ModeusEvent>,
        googleEvents: List<Event>
    ): Collection<CalendarRequest<*>> {
        val existingEvents = googleEvents
            .filter { event -> modeusEvents.any { event.description.contains(it.id.toString()) } }

        return existingEvents.map { event: Event ->
            val modeusEvent = modeusEvents.first { event.description.contains(it.id.toString()) }
            val updatedEvent = GoogleEventFactory.create(context, modeusEvent)
            calendar.events().update(CALENDAR_ID, event.id, updatedEvent)
        }
    }

    private fun getDeleteRequests(
        modeusEvents: List<ModeusEvent>,
        googleEvents: List<Event>
    ): Collection<CalendarRequest<*>> {
        val eventsToDelete = googleEvents
            .filter { event -> modeusEvents.none { event.description.contains(it.id.toString()) } }

        return eventsToDelete.map { calendar.events().delete(CALENDAR_ID, it.id) }
    }

    private fun getInsertRequests(
        modeusEvents: List<ModeusEvent>,
        googleEvents: List<Event>
    ): Collection<CalendarRequest<*>> {
        val eventsToCreate = modeusEvents
            .filter { modeusEvent -> googleEvents.none { it.description.contains(modeusEvent.id.toString()) } }

        return eventsToCreate
            .map { GoogleEventFactory.create(context, it) }
            .map { calendar.events().insert(CALENDAR_ID, it) }
    }

    private fun executeAll(requests: Collection<CalendarRequest<*>>) {
        requests.forEach {
            it.execute()
        }
    }
}
