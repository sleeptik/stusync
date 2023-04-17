package com.stusyncteam.stusync.api.google

import com.google.api.services.calendar.Calendar
import com.stusyncteam.stusync.api.modeus.models.Lesson


class CalendarFacade(private val calendar: Calendar) {
    fun transformLessonsAndUploadEvents(lessons: List<Lesson>) {
        val requests = lessons.map {
            val event = LessonEventBuilder(it)
                .setDefaultSummary()
                .setDefaultDescription()
                .setDefaultReminders()
                .setDefaultDates()
                .event

            calendar.events().insert("primary", event)
        }


        val thread = Thread { requests.forEach { it.execute() } }
        thread.start()
        thread.join()
    }
}
