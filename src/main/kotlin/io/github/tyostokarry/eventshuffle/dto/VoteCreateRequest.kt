package io.github.tyostokarry.eventshuffle.dto

import java.time.LocalDate

data class VoteCreateRequest(
    val name: String,
    val votes: List<LocalDate>,
)
