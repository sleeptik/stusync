package com.stusyncteam.stusync.api.modeus.models

import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

data class Event(
    @SerializedName("id") val id: UUID,
    @SerializedName("name") val name: String,
    @SerializedName("nameShort") val shortName: String,
    @SerializedName("description") val description: String,
    @SerializedName("start") val start: Date,
    @SerializedName("end") val end: Date,
)