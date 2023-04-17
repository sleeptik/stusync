package com.stusyncteam.stusync.api.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import java.io.File
import java.util.Collections

class CalendarFacadeFactory {
    companion object {
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"
        private const val TOKENS_DIRECTORY_PATH = "/tokens"

        private const val DATA_STORE_ACCESS_TYPE = "offline"
        private const val RECEIVER_PORT = 8888

        private val CALENDAR_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS)
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()

        fun createUserCalendar(userId: String): CalendarFacade {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val credentials = getCredentials(transport, userId)
            val calendar = Calendar.Builder(transport, JSON_FACTORY, credentials).build()
            return CalendarFacade(calendar)
        }


        private fun getCredentials(httpTransport: HttpTransport, userId: String): Credential {
            val reader = Companion::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)!!.reader()
            val secrets = GoogleClientSecrets.load(JSON_FACTORY, reader)
            val flow = getAuthorizationGoogleFlow(httpTransport, secrets)
            val receiver = LocalServerReceiver.Builder().setPort(RECEIVER_PORT).build()
            return AuthorizationCodeInstalledApp(flow, receiver).authorize(userId)
        }


        private fun getAuthorizationGoogleFlow(
            transport: HttpTransport, secrets: GoogleClientSecrets?
        ): GoogleAuthorizationCodeFlow? {
            val foo = FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH))
            return GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, secrets, CALENDAR_SCOPES
            ).setDataStoreFactory(foo).setAccessType(DATA_STORE_ACCESS_TYPE).build()
        }
    }
}