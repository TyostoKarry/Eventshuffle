package io.github.tyostokarry.eventshuffle.exception

class EventNotFoundException(
    id: Long,
) : RuntimeException("Event with ID $id not found")
