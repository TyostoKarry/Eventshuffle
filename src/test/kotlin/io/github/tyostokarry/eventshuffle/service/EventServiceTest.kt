package io.github.tyostokarry.eventshuffle.service

import io.github.tyostokarry.eventshuffle.dto.VoteCreateRequest
import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.entity.EventVote
import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import io.github.tyostokarry.eventshuffle.repository.EventRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventServiceTest {
    private val eventRepository: EventRepository = mock()
    private val service = EventService(eventRepository)

    @Nested
    inner class GetAllEvents {
        @Test
        fun `getAllEvents returns all all available events`() {
            val event1 = Event(id = 1L, name = "Test Event 1", dates = listOf(LocalDate.of(2025, 10, 5)))
            val event2 = Event(id = 2L, name = "Test Event 2", dates = listOf(LocalDate.of(2025, 10, 6)))

            given(eventRepository.findAll()).willReturn(listOf(event1, event2))

            val result = service.getAllEvents()

            assertEquals(2, result.size, "Expected two events to be returned")
            assertEquals(listOf(event1, event2), result, "Returned list should match repository output")
        }
    }

    @Nested
    inner class GetEventById {
        @Test
        fun `getEventById returns event when found`() {
            val event =
                Event(
                    id = 1L,
                    name = "Test Event",
                    dates =
                        listOf(
                            LocalDate.of(2025, 10, 5),
                            LocalDate.of(2025, 10, 6),
                        ),
                )
            val vote =
                EventVote(
                    id = 1L,
                    voterName = "Test Name",
                    votedDates = listOf(LocalDate.of(2025, 10, 5)),
                    event = event,
                )
            event.votes.add(vote)

            given(eventRepository.findById(1L)).willReturn(Optional.of(event))

            val result = service.getEventById(1L)

            assertEquals(1L, result.id, "Response event id should be 1")
            assertEquals("Test Event", result.name, "Response event name should be 'Test Event'")
            assertEquals(2, result.dates.size, "Response event should contain exactly 2 dates")
            assertEquals(
                listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6)),
                result.dates,
                "Response event dates should be '2025-10-5' and '2025-10-6'",
            )
            assertEquals(1, result.votes.size, "Response event should contain exactly 1 vote")
            assertEquals("Test Name", result.votes.first().voterName, "Response event vote should be from person 'Test Name'")
            assertEquals(
                listOf(LocalDate.of(2025, 10, 5)),
                result.votes.first().votedDates,
                "Response event vote date should be '2025-10-5'",
            )
        }

        @Test
        fun `getEventById throws EventNotFoundException when event does not exist`() {
            given(eventRepository.findById(999L)).willReturn(Optional.empty())

            assertThrows<EventNotFoundException>(
                message = "Expected EventNotFoundException when requesting invalid event id",
            ) { service.getEventById(999L) }
        }
    }

    @Nested
    inner class SaveEvent {
        @Test
        fun `saveEvent creates new event correctly`() {
            val unsaved = Event(name = "Test Event", dates = listOf(LocalDate.of(2025, 10, 5)))
            val saved = unsaved.copy(id = 99L)

            given(eventRepository.save(any<Event>())).willReturn(saved)

            val result = service.saveEvent(unsaved)

            assertEquals(99, result.id, "Response saved event id should be 99")
            assertEquals("Test Event", result.name, "Response saved event name should be 'Test Event'")
            assertEquals(listOf(LocalDate.of(2025, 10, 5)), result.dates, "Response saved event dates should be '2025-10-5'")
            assertEquals(emptyList(), result.votes, "Response saved event votes list should be empty")
        }
    }

    @Nested
    inner class AddVote {
        @Test
        fun `addVote adds new vote correctly`() {
            val event =
                Event(
                    id = 1L,
                    name = "Test Event",
                    dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6)),
                )

            given(eventRepository.findById(1L)).willReturn(Optional.of(event))
            given(eventRepository.save(any<Event>())).willAnswer { it.arguments[0] }

            val request = VoteCreateRequest("Test Name", listOf(LocalDate.of(2025, 10, 5)))
            val result = service.addVote(1L, request)

            assertEquals(1, result.votes.size, "Response event should contain exactly 1 vote")
            assertEquals("Test Name", result.votes.first().voterName, "Response event vote should be from 'Test Name'")
            assertTrue(
                result.votes
                    .first()
                    .votedDates
                    .contains(LocalDate.of(2025, 10, 5)),
                "Response event vote should be for the date 1015-10-05",
            )
        }

        @Test
        fun `addVote allows multiple persons to vote on event`() {
            val event =
                Event(
                    id = 1L,
                    name = "Test Event",
                    dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6)),
                )

            given(eventRepository.findById(1L)).willReturn(Optional.of(event))
            given(eventRepository.save(any<Event>())).willAnswer { it.arguments[0] }

            val firstRequest = VoteCreateRequest("Test Name 1", listOf(LocalDate.of(2025, 10, 5)))
            val secondRequest = VoteCreateRequest("Test Name 2", listOf(LocalDate.of(2025, 10, 5)))
            val thirdRequest = VoteCreateRequest("Test Name 3", listOf(LocalDate.of(2025, 10, 6)))

            val firstResponse = service.addVote(1L, firstRequest)
            assertEquals(1, firstResponse.votes.size, "First response event should contain exactly 1 vote")
            val secondResponse = service.addVote(1L, secondRequest)
            assertEquals(2, secondResponse.votes.size, "Second response event should contain exactly 2 votes")
            val thirdResponse = service.addVote(1L, thirdRequest)
            assertEquals(3, thirdResponse.votes.size, "Third response event should contain exactly 3 votes")

            val votesByDate =
                thirdResponse.votes
                    .flatMap { vote -> vote.votedDates.map { date -> date to vote.voterName } }
                    .groupBy({ it.first }, { it.second })
            assertEquals(
                setOf("Test Name 1", "Test Name 2"),
                votesByDate[LocalDate.of(2025, 10, 5)]?.toSet(),
                "Expected voters on 2025‑10‑05 to be 'Test Name 1' and 'Test Name 2'",
            )
            assertEquals(
                setOf("Test Name 3"),
                votesByDate[LocalDate.of(2025, 10, 6)]?.toSet(),
                "Expected voter on 2025‑10‑06 to be 'Test Name 3'",
            )
        }

        @Test
        fun `addVote replaces previous vote by same person`() {
            val event =
                Event(
                    id = 1L,
                    name = "Test Event",
                    dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6)),
                )

            given(eventRepository.findById(1L)).willReturn(Optional.of(event))
            given(eventRepository.save(any<Event>())).willAnswer { it.arguments[0] }

            val initialVote =
                VoteCreateRequest(
                    name = "Test Name",
                    votes = listOf(LocalDate.of(2025, 10, 5)),
                )

            val initialResponse = service.addVote(1L, initialVote)

            assertEquals(1, initialResponse.votes.size, "Initial response event should contain exactly 1 vote")
            assertEquals(
                "Test Name",
                initialResponse.votes.first().voterName,
                "Initial response event vote should be from 'Test Name'",
            )
            assertEquals(
                listOf(LocalDate.of(2025, 10, 5)),
                initialResponse.votes.first().votedDates,
                "Initial response event vote should be for the date 1015-10-05",
            )

            val updatedVote =
                VoteCreateRequest(
                    name = "Test Name",
                    votes = listOf(LocalDate.of(2025, 10, 6)),
                )

            val updatedResponse = service.addVote(1L, updatedVote)

            assertEquals(1, updatedResponse.votes.size, "Updated response event should contain exactly 1 vote")
            assertEquals(
                "Test Name",
                updatedResponse.votes.first().voterName,
                "Updated response event vote should be from 'Test Name'",
            )
            assertEquals(
                listOf(LocalDate.of(2025, 10, 6)),
                updatedResponse.votes.first().votedDates,
                "Updated response event vote should be for the date 1015-10-06",
            )
        }

        @Test
        fun `addVote throws when event is not found`() {
            given(eventRepository.findById(999L)).willReturn(Optional.empty())

            val request = VoteCreateRequest("Test Name", listOf(LocalDate.of(2025, 10, 5)))

            assertThrows<EventNotFoundException>(
                message = "Expected EventNotFoundException when event is missing",
            ) { service.addVote(999L, request) }
        }

        @Test
        fun `addVote throws when invalid dates are provided`() {
            val event =
                Event(
                    id = 1L,
                    name = "Test Event",
                    dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 6)),
                )

            given(eventRepository.findById(1L)).willReturn(Optional.of(event))

            val request = VoteCreateRequest("Test Name", listOf(LocalDate.of(2025, 12, 31)))

            assertThrows<IllegalArgumentException>(
                message = "Expected IllegalArgumentException when invalid dates are provided",
            ) { service.addVote(1L, request) }
        }
    }
}
