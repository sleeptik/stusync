package com.stusyncteam.stusync.background

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager

class AutoSyncWorkScheduler {
    companion object {
        private const val WORK_NAME = "stusync_auto_sync"

        fun scheduleAutoSync(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val autoSyncRequest = BackgroundSyncWorker.createPeriodicWorkRequest(15)

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                autoSyncRequest
            )
        }

        fun disableAutoSync(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }
}