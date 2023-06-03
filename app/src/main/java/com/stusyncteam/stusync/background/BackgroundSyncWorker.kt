package com.stusyncteam.stusync.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.modeus.api.auth.UserCredentials
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BackgroundSyncWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val credentialsStorage = CredentialsStorage(context)

        runBlocking {
            withContext(Dispatchers.IO) {
                val credentials: UserCredentials? = credentialsStorage.load()
                val session = ModeusSignIn.login(credentials!!)

                val self = session.getMyself()
                val events = session.getPersonEvents(self)

                val googleCalendar = GoogleCalendarFacade.fromContext(context)
                googleCalendar.updateCalendar(events)
            }
        }

        return Result.success()
    }

    companion object {
        fun createPeriodicWorkRequest(minutesInterval: Long): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            return PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
                minutesInterval,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
        }
    }
}