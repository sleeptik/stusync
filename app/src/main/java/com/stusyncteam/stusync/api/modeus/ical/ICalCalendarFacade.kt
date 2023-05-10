package com.stusyncteam.stusync.api.modeus.ical

import com.stusyncteam.stusync.api.modeus.models.Lesson
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import java.io.InputStream

class ICalCalendarFacade private constructor(private val calendar: Calendar) {
    companion object {
        fun fromStream(stream: InputStream): ICalCalendarFacade {
            val contents = stream.bufferedReader().use {
                it.readText()
            }

            val timezoneIdRegex = Regex(";TZID=\\w+/\\w+")
            val filteredContents = contents.replace(timezoneIdRegex, "")

            filteredContents.reader().use {
                val calendar = CalendarBuilder().build(it)
                return ICalCalendarFacade(calendar)
            }
        }
    }

    val lessons: List<Lesson> get() = getLessons()

    fun getLessons(): List<Lesson> {
        return calendar.getComponents<VEvent>("VEVENT")
            .map {
                Lesson(
                    it.summary.value,
                    it.location.value,
                    "",
                    it.startDate.date,
                    it.endDate.date
                )
            }
    }
}