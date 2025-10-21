package io.github.tyostokarry.eventshuffle.dto

import java.time.LocalDate

data class VotesListItemDto(
    val date: LocalDate,
    val people: List<String>,
)
