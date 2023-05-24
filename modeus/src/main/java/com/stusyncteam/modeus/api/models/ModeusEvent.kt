package com.stusyncteam.modeus.api.models

import java.util.Date
import java.util.UUID

data class ModeusEvent(
    val id: UUID,
    val name: String,
    val building: String,
    val classroom: String,
    val start: Date,
    val end: Date
)