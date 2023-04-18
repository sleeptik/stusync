package com.stusyncteam.stusync.api.modeus.models

import com.google.api.client.util.DateTime
import java.util.Date

class MockCollections {
    companion object {
        fun createLessons(): List<Lesson> {
            val start = DateTime(Date())

            val date = Date()
            date.time += 1000 * 60 * 60 * 2
            val end = DateTime(date)

            return listOf(
                Lesson("test1", "class1", "building1", start, end)
            )
        }
    }
}