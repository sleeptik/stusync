package com.stusyncteam.stusync.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stusyncteam.stusync.R

class NotificationFacade(private val context: Context) {
    companion object {
        private const val notificationId = 0
        private const val notificationChannelId = "stusync"
        private const val notificationChannelName = "notifications"
        private const val notificationImportance = NotificationManagerCompat.IMPORTANCE_DEFAULT
        private const val notificationPriority = NotificationCompat.PRIORITY_DEFAULT
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    // TODO request permission somewhere
    @SuppressLint("MissingPermission")
    fun makeNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasNotificationChannel()) {
            createNotificationChannel()
        }

        notificationManager.notify(notificationId, createNotification())
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.baseline_notifications_active_24)
            .setContentTitle("Study schedule changed")
            .setPriority(notificationPriority)
            .build()
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannelCompat.Builder(
            notificationChannelId,
            notificationImportance
        )
            .setName(notificationChannelName)
            .build()
        notificationManager.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hasNotificationChannel(): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        return notificationManager.getNotificationChannel(notificationChannelId) != null
    }
}