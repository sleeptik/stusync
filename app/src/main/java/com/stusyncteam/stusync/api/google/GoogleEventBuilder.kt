package com.stusyncteam.stusync.api.google

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Event.Reminders
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.stusyncteam.modeus.api.models.ModeusEvent

class GoogleEventBuilder(private val modeusEvent: ModeusEvent) {
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
            .appendLine("${modeusEvent.building} ${modeusEvent.classroom}")
            .appendLine("lesson type")
            .appendLine("ID=${modeusEvent.id}")
            .toString()

        event.description = description

        return this
    }

    fun setDefaultDates(): GoogleEventBuilder {
        event.start = EventDateTime().setDateTime(DateTime(modeusEvent.start))
        event.end = EventDateTime().setDateTime(DateTime(modeusEvent.end))

        return this
    }

    fun setDefaultSummary(): GoogleEventBuilder {
        event.summary = modeusEvent.name

        return this
    }
}