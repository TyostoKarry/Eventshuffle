package io.github.tyostokarry.eventshuffle.service

import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.repository.EventRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EventService(
    private val eventRepository: EventRepository,
) {
    fun getAllEvents(): List<Event> = eventRepository.findAll()

    fun getEventById(id: Long): Event? = eventRepository.findByIdOrNull(id)

    fun saveEvent(event: Event): Event = eventRepository.save(event)
}
