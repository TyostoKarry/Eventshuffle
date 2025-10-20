package io.github.tyostokarry.eventshuffle.dto

import java.time.LocalDate

data class EventDetailsResponse(
    val id: Long,
    val name: String,
    val dates: List<LocalDate>,
)
