package io.github.tyostokarry.eventshuffle.dto

import io.github.tyostokarry.eventshuffle.entity.Event
import java.time.LocalDate

data class EventDetailsResponse(
    val id: Long,
    val name: String,
    val dates: List<LocalDate>,
    val votes: List<VotesListItemDto>,
) {
    companion object {
        fun fromEvent(event: Event): EventDetailsResponse {
            val grouped = mutableMapOf<LocalDate, MutableList<String>>()

            event.votes.forEach { vote ->
                vote.votedDates.forEach { date ->
                    grouped.computeIfAbsent(date) { mutableListOf() }.add(vote.voterName)
                }
            }

            val votes =
                grouped
                    .map { (date, people) ->
                        VotesListItemDto(date, people)
                    }.sortedBy { it.date }

            return EventDetailsResponse(
                id = event.id,
                name = event.name,
                dates = event.dates,
                votes = votes,
            )
        }
    }
}
