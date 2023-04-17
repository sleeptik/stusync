package com.stusyncteam.stusync.api.google

import com.google.api.services.calendar.Calendar
import com.stusyncteam.stusync.api.modeus.models.Lesson


class CalendarFacade(private val calendar: Calendar) {
    fun transformLessonsAndUploadEvents(lessons: List<Lesson>) {
        val events = lessons.map {
            LessonEventBuilder(it)
                .setDefaultSummary()
                .setDefaultDescription()
                .setDefaultReminders()
                .setDefaultDates()
                .event
        }

        events.forEach { calendar.events().insert("primary", it) }
    }
}
