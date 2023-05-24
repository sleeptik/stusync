package com.stusyncteam.modeus

import com.stusyncteam.modeus.api.models.ModeusEvent
import com.stusyncteam.modeus.api.models.ModeusPerson
import com.stusyncteam.modeus.api.repositories.EventRepository
import com.stusyncteam.modeus.api.repositories.PersonRepository
import okhttp3.OkHttpClient

class ModeusSession(
    private val httpClient: OkHttpClient,
    private val authToken: String
) {
    private val personRepository: PersonRepository = PersonRepository()
    private val eventRepository: EventRepository = EventRepository()

    fun getPersonByName(name: String): ModeusPerson {
        // TODO catch network, auth exceptions
        return personRepository.getPersonByName(httpClient, authToken, name)
    }

    fun getPersonEvents(person: ModeusPerson): List<ModeusEvent> {
        // TODO catch network, auth exceptions
        return eventRepository.getPersonEvents(httpClient, authToken, person)
    }
}