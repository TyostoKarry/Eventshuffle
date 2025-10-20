package io.github.tyostokarry.eventshuffle.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate

data class EventCreateRequest(
    @field:NotBlank(message = "Event name must not be blank")
    val name: String,
    @field:NotEmpty(message = "At least one date must be provided")
    val dates: List<LocalDate>,
)
