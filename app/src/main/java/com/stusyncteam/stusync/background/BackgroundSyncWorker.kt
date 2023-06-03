package com.stusyncteam.stusync.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.stusyncteam.modeus.api.auth.ModeusSignIn
import com.stusyncteam.modeus.api.auth.UserCredentials
import com.stusyncteam.stusync.api.google.GoogleCalendarFacade
import com.stusyncteam.stusync.storage.credentials.CredentialsStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class BackgroundSyncWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // TODO remove duplicate code of manual sync
        val credentialsStorage = CredentialsStorage(context)

        val credentials: UserCredentials = credentialsStorage.load()
            ?: return@withContext Result.failure()

        val session = ModeusSignIn.login(credentials)

        val self = session.getMyself()
        val events = session.getPersonEvents(self)

        val googleCalendar = GoogleCalendarFacade.fromContext(context)
        googleCalendar.updateCalendar(events)

        return@withContext Result.success()
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