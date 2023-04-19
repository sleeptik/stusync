package com.stusyncteam.stusync.api.modeus.ical

import com.stusyncteam.stusync.api.modeus.models.Lesson
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import java.io.InputStream
import java.io.StringReader

class ICalCalendarFacade private constructor(private val calendar: Calendar) {
    companion object {
        fun fromStream(stream: InputStream): ICalCalendarFacade {
            val formattedStream = formatTimeStampsAndGetStream(stream)
            formattedStream.use {
                return ICalCalendarFacade(CalendarBuilder().build(it))
            }
        }

        private fun formatTimeStampsAndGetStream(stream: InputStream): StringReader {
            // TODO: Replace with regex to support different timezones
            val toReplace = ";TZID=Asia/Yekaterinburg"
            val replaceWith = ""
            return stream.bufferedReader()
                .readText()
                .replace(toReplace, replaceWith)
                .reader()
        }
    }

    fun getLessons(): List<Lesson> {
        val components = calendar.getComponents<VEvent>("VEVENT")

        val lessons = components.map {
            Lesson(
                it.summary.value,
                it.location.value,
                "exclude",
                it.startDate.date,
                it.endDate.date
            )
        }

        return lessons
    }
}