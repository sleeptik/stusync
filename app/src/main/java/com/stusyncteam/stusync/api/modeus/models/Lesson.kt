package com.stusyncteam.stusync.api.modeus.models

import com.google.api.client.util.DateTime

data class Lesson(
    val name: String,
    val classroom: String,
    val building: String,
    val startDate: DateTime,
    val endDate: DateTime
)




