package com.stusyncteam.stusync.api.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Event.Reminders
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.stusyncteam.stusync.api.modeus.models.Lesson

class GoogleEventBuilder(private val lesson: Lesson) {
    val event: Event = Event()

    fun setDefaultReminders(): GoogleEventBuilder {
        val reminders = listOf(
            EventReminder().setMethod("popup").setMinutes(10),
            EventReminder().setMethod("popup").setMinutes(30)
        )

        event.reminders = Reminders()
            .setUseDefault(false)
            .setOverrides(reminders)

        return this
    }

    fun setDefaultDescription(): GoogleEventBuilder {
        val description = StringBuilder()
            .appendLine("${lesson.building} ${lesson.classroom}")
            .appendLine("lesson type")
            .toString()

        event.description = description

        return this
    }

    fun setDefaultDates(): GoogleEventBuilder {
        event.start = EventDateTime().setDateTime(DateTime(lesson.startDate))
        event.end = EventDateTime().setDateTime(DateTime(lesson.endDate))

        return this
    }

    fun setDefaultSummary(): GoogleEventBuilder {
        event.summary = lesson.name

        return this
    }
}