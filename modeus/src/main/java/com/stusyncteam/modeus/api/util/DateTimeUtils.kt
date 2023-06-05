package com.stusyncteam.modeus.api.util

import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.ReadableDateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.Date

 class DateTimeUtils {
    companion object {
        fun getSyncStartDate(): DateTime {
            return LocalDateTime.now()
                .withDayOfWeek(1)
                .withTime(0, 0, 0, 0)
                .toDateTime()
        }

        fun getSyncEndDate(): DateTime {
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