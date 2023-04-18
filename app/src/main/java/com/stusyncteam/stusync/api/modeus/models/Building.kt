package com.stusyncteam.stusync.api.modeus.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Building(
    @SerializedName("id") val id: UUID,
    @SerializedName("name") val name: String,
    @SerializedName("nameShort") val shortName: String,
    @SerializedName("address") val address: String,
    @SerializedName("searchableAddress") val searchableAddress: String,
    @SerializedName("displayOrder") val displayOrder: Int
)