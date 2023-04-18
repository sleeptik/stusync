package com.stusyncteam.stusync.api.google

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import java.util.Collections

class CalendarFacadeFactory {
    companion object {
        private const val applicationName = "stusync"

        fun createCalendarFacade(context: Context): CalendarFacade {
            val scopes = Collections.singleton(CalendarScopes.CALENDAR)
            val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
            credential.selectedAccount = credential.googleAccountManager.accounts.first()

            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val calendar = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build()

            return CalendarFacade(calendar)
        }
    }
}