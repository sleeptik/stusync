package com.stusyncteam.modeus.api.models

import java.util.UUID

data class ModeusPerson(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val middleName: String,
)