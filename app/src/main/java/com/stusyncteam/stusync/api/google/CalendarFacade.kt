package com.stusyncteam.stusync.api.google

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarRequest
import com.stusyncteam.stusync.api.modeus.models.Lesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CalendarFacade(private val calendar: Calendar) {
    fun prepareRequests(lessons: List<Lesson>): Collection<CalendarRequest<*>> {
        val requests = lessons.map {
            val event = LessonEventBuilder(it)
                .setDefaultSummary()
                .setDefaultDescription()
                .setDefaultReminders()
                .setDefaultDates()
                .event

            calendar.events().insert("primary", event)
        }

        return requests.map { it as CalendarRequest<*> }
    }

    suspend fun executeAll(requests: Collection<CalendarRequest<*>>) {
        withContext(Dispatchers.IO) {
            requests.forEach { it.execute() }
        }
    }
}
