package com.stusyncteam.stusync.api.modeus.models

import java.util.Date

data class Lesson(
    val name: String,
    val classroom: String,
    val building: String,
    val startDate: Date,
    val endDate: Date
)




