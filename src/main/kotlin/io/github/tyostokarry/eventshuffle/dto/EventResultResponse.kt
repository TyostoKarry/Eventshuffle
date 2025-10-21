package io.github.tyostokarry.eventshuffle.dto

import io.github.tyostokarry.eventshuffle.entity.Event

data class EventResultResponse(
    val id: Long,
    val name: String,
    val suitableDates: List<VotesListItemDto>,
) {
    companion object {
        fun fromEvent(event: Event): EventResultResponse {
            if (event.votes.isEmpty()) {
                return EventResultResponse(
                    id = event.id,
                    name = event.name,
                    suitableDates = emptyList(),
                )
            }

            val allVoters = event.votes.map { it.voterName }.toSet()

            val suitableDates =
                event.dates
                    .filter { date ->
                        event.votes.all {
                            date in it.votedDates
                        }
                    }.map { date ->
                        VotesListItemDto(
                            date = date,
                            people = allVoters.toList(),
                        )
                    }

            return EventResultResponse(
                id = event.id,
                name = event.name,
                suitableDates = suitableDates,
            )
        }
    }
}
