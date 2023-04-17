package com.stusyncteam.stusync.api.modeus.models

import com.google.api.client.util.DateTime
import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Lesson(
    val name: String,
    val classroom: String,
    val building: String,
    val startDate: DateTime,
    val endDate: DateTime
)




