package com.stusyncteam.modeus.api.util

import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.ReadableDateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.Date

internal class DateTimeUtils {
    companion object {
        fun getStartOfTheWeek(): DateTime {
            return LocalDateTime.now()
                .withDayOfWeek(1)
                .withTime(0, 0, 0, 0)
                .toDateTime()
        }

        fun getEndOfTheWeek(): DateTime {
            return LocalDateTime.now()
                .withDayOfWeek(1)
                .withTime(0, 0, 0, 0)
                .plusWeeks(1)
                .toDateTime()
        }

        fun stringToDate(date: String): Date {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(date).toDate()
        }

        fun dateTimeToString(dateTime: ReadableDateTime): String {
            return ISODateTimeFormat.dateTimeNoMillis().print(dateTime)
        }
    }
}