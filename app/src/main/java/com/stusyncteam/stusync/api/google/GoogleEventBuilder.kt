package com.stusyncteam.stusync.api.google

import android.content.Context
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Event.Reminders
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.stusyncteam.modeus.api.models.ModeusEvent
import com.stusyncteam.stusync.storage.settings.ImportSettings
import com.stusyncteam.stusync.storage.settings.ImportSettingsStorage
import com.stusyncteam.stusync.storage.settings.NotificationSettings
import com.stusyncteam.stusync.storage.settings.NotificationSettingsStorage
import kotlinx.coroutines.runBlocking


class GoogleEventFactory {
    companion object {
        const val MODEUS_UUID_PREFIX = "modeus-"

        fun create(context: Context, modeusEvent: ModeusEvent): Event {
            val importSettings = runBlocking {
                ImportSettingsStorage(context).load() ?: ImportSettings()
            }
            val notificationSettings = runBlocking {
                NotificationSettingsStorage(context).load() ?: NotificationSettings()
            }

            val builder = GoogleEventBuilder(modeusEvent, importSettings)
                .setSummary()
                .setDescription()
                .setDates()

            if (notificationSettings.shouldNotifyBeforeNextLessonStarts)
                builder.setReminders()
            else
                builder.removeReminders()

            return builder.event
        }
    }

    private class GoogleEventBuilder(
        private val modeusEvent: ModeusEvent,
        private val importSettings: ImportSettings,
    ) {
        val event: Event = Event()

        fun setReminders(): GoogleEventBuilder {
            val remindersOverrides = listOf(
                EventReminder().setMethod("popup").setMinutes(15),
                EventReminder().setMethod("popup").setMinutes(105)
            )

            event.reminders = Reminders()
                .setUseDefault(false)
                .setOverrides(remindersOverrides)

            return this
        }

        fun removeReminders(): GoogleEventBuilder {
            event.reminders = Reminders()
                .setUseDefault(false)

            return this
        }

        fun setDescription(): GoogleEventBuilder {
            val description = StringBuilder()
                .appendLine("${modeusEvent.building}")
                .appendLine("${modeusEvent.classroom}")

            if (importSettings.shouldImportLessonType)
                description.appendLine(modeusEvent.lessonType)

            if (importSettings.shouldImportTeacherName) {
                description.appendLine()
                description.appendLine("Teachers") //TODO localize
                modeusEvent.teachers.forEach { description.appendLine(it.fullName) }
            }

            description
                .appendLine()
                .appendLine("${MODEUS_UUID_PREFIX}${modeusEvent.id}")

            event.description = description.toString()

            return this
        }

        fun setDates(): GoogleEventBuilder {
            event.start = EventDateTime().setDateTime(DateTime(modeusEvent.start))
            event.end = EventDateTime().setDateTime(DateTime(modeusEvent.end))

            return this
        }

        fun setSummary(): GoogleEventBuilder {
            event.summary = modeusEvent.name

            return this
        }
    }
}