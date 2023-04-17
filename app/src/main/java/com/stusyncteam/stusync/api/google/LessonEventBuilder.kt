package com.stusyncteam.stusync.api.google

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Event.Reminders
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.stusyncteam.stusync.api.modeus.models.Lesson

class LessonEventBuilder(private val lesson: Lesson) {
    val event: Event = Event()

    fun setDefaultReminders(): LessonEventBuilder {
        val reminders = listOf(
            EventReminder().setMethod("popup").setMinutes(10),
            EventReminder().setMethod("popup").setMinutes(30)
        )

        event.reminders = Reminders().setOverrides(reminders)

        return this;
    }

    fun setDefaultDescription(): LessonEventBuilder {
        val description = StringBuilder()
            .appendLine("${lesson.building} ${lesson.classroom}")
            .appendLine("lesson type")
            .toString()

        event.description = description

        return this;
    }

    fun setDefaultDates(): LessonEventBuilder {
        event.start = EventDateTime().setDateTime(lesson.startDate)
        event.end = EventDateTime().setDateTime(lesson.endDate)

        return this
    }

    fun setDefaultSummary(): LessonEventBuilder {
        event.summary = lesson.name

        return this
    }
}