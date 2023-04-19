package com.stusyncteam.stusync.api.modeus.models

import java.util.Date

class MockCollections {
    companion object {
        fun createLessons(): List<Lesson> {
            val startDate = Date()
            val endDate = Date()
            endDate.time += 1000 * 60 * 60 * 2

            return listOf(
                Lesson("test1", "class1", "building1", startDate, endDate)
            )
        }
    }
}