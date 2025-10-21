package io.github.tyostokarry.eventshuffle.controller

import io.github.tyostokarry.eventshuffle.dto.EventCreateRequest
import io.github.tyostokarry.eventshuffle.dto.EventCreateResponse
import io.github.tyostokarry.eventshuffle.dto.EventDetailsResponse
import io.github.tyostokarry.eventshuffle.dto.EventListItemDto
import io.github.tyostokarry.eventshuffle.dto.EventListResponse
import io.github.tyostokarry.eventshuffle.dto.VoteCreateRequest
import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.service.EventService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/event")
class EventController(
    private val eventService: EventService,
) {
    @GetMapping("/list")
    fun getAllEvents(): ResponseEntity<EventListResponse> {
        val events = eventService.getAllEvents()
        val items = events.map { EventListItemDto(it.id, it.name) }
        return ResponseEntity.ok(EventListResponse(items))
    }

    @GetMapping("/{id}")
    fun getEventById(
        @PathVariable id: Long,
    ): ResponseEntity<EventDetailsResponse> {
        val event = eventService.getEventById(id)
        return ResponseEntity.ok(EventDetailsResponse.fromEvent(event))
    }

    @PostMapping
    fun createEvent(
        @Valid @RequestBody request: EventCreateRequest,
    ): ResponseEntity<EventCreateResponse> {
        val event = Event(name = request.name, dates = request.dates)
        val saved = eventService.saveEvent(event)
        val response = EventCreateResponse(saved.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{id}/vote")
    fun voteOnEvent(
        @PathVariable id: Long,
        @RequestBody request: VoteCreateRequest,
    ): ResponseEntity<EventDetailsResponse> {
        val response = eventService.addVote(id, request)
        return ResponseEntity.ok(response)
    }
}
