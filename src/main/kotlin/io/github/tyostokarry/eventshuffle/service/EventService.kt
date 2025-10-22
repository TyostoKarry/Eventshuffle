package io.github.tyostokarry.eventshuffle.service

import io.github.tyostokarry.eventshuffle.dto.VoteCreateRequest
import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.entity.EventVote
import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import io.github.tyostokarry.eventshuffle.repository.EventRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EventService(
    private val eventRepository: EventRepository,
) {
    fun getAllEvents(): List<Event> = eventRepository.findAll()

    fun getEventById(id: Long): Event = eventRepository.findByIdOrNull(id) ?: throw EventNotFoundException(id)

    fun saveEvent(event: Event): Event = eventRepository.save(event)

    @Transactional
    fun addVote(
        eventId: Long,
        request: VoteCreateRequest,
    ): Event {
        val event = getEventById(eventId)

        val invalidDates = request.votes.filterNot { it in event.dates }
        if (invalidDates.isNotEmpty()) {
            val expected = event.dates.joinToString(", ")
            val provided = request.votes.joinToString(", ")
            val invalid = invalidDates.joinToString(", ")
            throw IllegalArgumentException(
                "Invalid vote date(s) for event '${event.name}' (ID: $eventId): " +
                    "Provided = [$provided], Allowed = [$expected], Invalid = [$invalid]",
            )
        }

        // If participant has voted previously, remove old vote
        event.votes.removeIf { it.voterName.equals(request.name, ignoreCase = true) }

        if (request.votes.isNotEmpty()) {
            val newVote =
                EventVote(
                    voterName = request.name,
                    votedDates = request.votes,
                    event = event,
                )
            event.votes.add(newVote)
        }

        val saved = eventRepository.save(event)
        return saved
    }
}
