package io.github.tyostokarry.eventshuffle.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.tyostokarry.eventshuffle.dto.EventCreateRequest
import io.github.tyostokarry.eventshuffle.dto.EventCreateResponse
import io.github.tyostokarry.eventshuffle.dto.EventDetailsResponse
import io.github.tyostokarry.eventshuffle.dto.EventListResponse
import io.github.tyostokarry.eventshuffle.dto.EventResultResponse
import io.github.tyostokarry.eventshuffle.dto.VoteCreateRequest
import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.entity.EventVote
import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import io.github.tyostokarry.eventshuffle.service.EventService
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.text.contains

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
) {
    @MockitoBean
    lateinit var eventService: EventService

    @Nested
    inner class ListAllEvents {
        @Test
        fun `get all events returns list`() {
            val events =
                listOf(
                    Event(id = 1, name = "Test Event 1", dates = listOf(LocalDate.of(2025, 6, 1))),
                    Event(id = 2, name = "Test Event 2", dates = listOf(LocalDate.of(2025, 6, 2))),
                )
            given(eventService.getAllEvents()).willReturn(events)

            val responseBody =
                mockMvc
                    .get("/api/v1/event/list")
                    .andExpect { status { isOk() } }
                    .andReturn()
                    .response.contentAsString

            val response = objectMapper.readValue(responseBody, EventListResponse::class.java)

            assertEquals(2, response.events.size, "The list should contain exactly two events")

            val first = response.events[0]
            val second = response.events[1]

            assertEquals(1, first.id, "First event ID should be 1")
            assertEquals("Test Event 1", first.name, "First event name should be 'Test Event 1'")

            assertEquals(2, second.id, "Second event ID should be 2")
            assertTrue(second.name.startsWith("Test Event 2"), "Second event name should start with 'Test Event 2'")
        }
    }

    @Nested
    inner class GetEventById {
        @Test
        fun `get event by id returns full details`() {
            val event = Event(id = 42, name = "Test Event", dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 12)))
            given(eventService.getEventById(42)).willReturn(event)

            val responseBody =
                mockMvc
                    .get("/api/v1/event/42")
                    .andExpect { status { isOk() } }
                    .andReturn()
                    .response.contentAsString

            val response = objectMapper.readValue(responseBody, EventDetailsResponse::class.java)

            assertEquals(42, response.id, "Response event id should be 42")
            assertEquals("Test Event", response.name, "Response event name should be 'Test Event'")
            assertEquals(2, response.dates.size, "Response event should contain exactly 2 dates")
            assertEquals(LocalDate.of(2025, 10, 5), response.dates[0], "First response event date should be '2025-10-5'")
            assertEquals(LocalDate.of(2025, 10, 12), response.dates[1], "Second response event date should be '2025-10-12'")
            assertEquals(0, response.votes.size, "Response event should not contain any votes")
        }

        @Test
        fun `get event by id returns 404 if not found`() {
            given(eventService.getEventById(999))
                .willThrow(EventNotFoundException(999))

            val responseBody =
                mockMvc
                    .get("/api/v1/event/999")
                    .andExpect { status { isNotFound() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertEquals("Event with ID 999 not found", responseBody, "Response body should be 'Event with ID 999 not found'")
        }
    }

    @Nested
    inner class CreateEvent {
        @Test
        fun `post creates event successfully`() {
            val request =
                EventCreateRequest(
                    name = "Test Event",
                    dates = listOf(LocalDate.of(2025, 12, 1)),
                )
            given(eventService.saveEvent(any())).willReturn(
                Event(id = 99L, name = request.name, dates = request.dates),
            )

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }.andExpect { status { isCreated() } }
                    .andReturn()
                    .response
                    .contentAsString

            val response = objectMapper.readValue(responseBody, EventCreateResponse::class.java)

            assertEquals(99, response.id, "Response event id should be 99")
        }

        @Test
        fun `post create event returns 400 when name is blank`() {
            val invalidRequest = EventCreateRequest(name = "   ", dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 12)))

            val body = objectMapper.writeValueAsString(invalidRequest)

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = body
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertNotNull(responseBody)
            assertTrue(
                responseBody.contains("Event name must not be blank"),
                "Error body should contain 'Event name must not be blank'.",
            )
        }

        @Test
        fun `post create event returns 400 when name is missing`() {
            val invalidJson =
                """
                {
                    "dates": ["2025-11-05", "2025-11-12"]
                }
                """.trimIndent()

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = invalidJson
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertNotNull(responseBody)
            assertTrue(
                responseBody.contains("value failed for JSON property name due to missing"),
                "Error body should contain 'value failed for JSON property name due to missing'.",
            )
        }

        @Test
        fun `post create event returns 400 when empty dates list is given`() {
            val invalidRequest = EventCreateRequest(name = "Test Event", dates = emptyList())

            val body = objectMapper.writeValueAsString(invalidRequest)

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = body
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertNotNull(responseBody)
            assertTrue(
                responseBody.contains("At least one date must be provided"),
                "Error body should contain 'At least one date must be provided'.",
            )
        }

        @Test
        fun `post create event returns 400 when dates are missing`() {
            val invalidJson =
                """
                {
                    "name": "Test Event"
                }
                """.trimIndent()

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = invalidJson
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertNotNull(responseBody)
            assertTrue(
                responseBody.contains(" value failed for JSON property dates due to missing"),
                "Error body should contain ' value failed for JSON property dates due to missing'.",
            )
        }

        @Test
        fun `post create event returns 400 when dates contain invalid format`() {
            val invalidJson =
                """
                {
                    "name": "Test Event",
                    "dates": ["not-a-date", "2025-11-12"]
                }
                """.trimIndent()

            val responseBody =
                mockMvc
                    .post("/api/v1/event") {
                        contentType = MediaType.APPLICATION_JSON
                        content = invalidJson
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertNotNull(responseBody)
            assertTrue(
                responseBody.contains("not-a-date") &&
                    responseBody.contains("Cannot deserialize value of type"),
                "Error body should contain an indication of invalid date format.",
            )
        }
    }

    @Nested
    inner class VoteOnEvent {
        @Test
        fun `vote on event returns updated details`() {
            val request =
                VoteCreateRequest(
                    name = "Test Name",
                    votes = listOf(LocalDate.of(2025, 10, 5)),
                )

            val event =
                Event(
                    id = 15L,
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

            given(eventService.addVote(eq(15L), any())).willReturn(event)

            val responseBody =
                mockMvc
                    .post("/api/v1/event/15/vote") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }.andExpect { status { isOk() } }
                    .andReturn()
                    .response
                    .contentAsString

            val response = objectMapper.readValue(responseBody, EventDetailsResponse::class.java)

            assertEquals(15, response.id, "Response event id should be 15")
            assertEquals("Test Event", response.name, "Response event name should be 'Test Event'")
            assertEquals(2, response.dates.size, "Response event should contain exactly 2 dates")
            assertEquals(1, response.votes.size, "Response event should contain exactly 1 vote")
            assertEquals(
                "Test Name",
                response.votes
                    .first()
                    .people
                    .first(),
                "Response event vote should be from 'Test Name'",
            )
            assertEquals(
                LocalDate.of(2025, 10, 5),
                response.votes.first().date,
                "Response event vote should be for the date 1015-10-05",
            )
        }

        @Test
        fun `post vote returns 404 when event is not found`() {
            val request =
                VoteCreateRequest(
                    name = "Test Name",
                    votes = listOf(LocalDate.of(2025, 10, 5)),
                )

            given(eventService.addVote(eq(999L), any()))
                .willThrow(EventNotFoundException(999))

            val responseBody =
                mockMvc
                    .post("/api/v1/event/999/vote") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }.andExpect { status { isNotFound() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertEquals(
                "Event with ID 999 not found",
                responseBody,
                "Error body should contain an indication of invalid event id.",
            )
        }

        @Test
        fun `post vote returns 400 when invalid dates are provided`() {
            val request =
                VoteCreateRequest(
                    name = "Test Name",
                    votes = listOf(LocalDate.of(2025, 10, 5)),
                )

            given(eventService.addVote(eq(1), any()))
                .willThrow(
                    IllegalArgumentException(
                        "Invalid vote date(s) for event 'My Event' (ID: 1): Provided = [2025-10-05], Allowed = [2025-11-05, 2025-11-12], Invalid = [2025-10-05]",
                    ),
                )

            val responseBody =
                mockMvc
                    .post("/api/v1/event/1/vote") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(request)
                    }.andExpect { status { isBadRequest() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertEquals(
                "Invalid vote date(s) for event 'My Event' (ID: 1): Provided = [2025-10-05], Allowed = [2025-11-05, 2025-11-12], Invalid = [2025-10-05]",
                responseBody,
                "Error body should contain an indication of invalid vote date.",
            )
        }
    }

    @Nested
    inner class GetResult {
        @Test
        fun `get event results returns aggregated vote summary`() {
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

            event.votes.addAll(
                listOf(
                    EventVote(
                        id = 1L,
                        voterName = "Test Name 1",
                        votedDates = listOf(LocalDate.of(2025, 10, 5)),
                        event = event,
                    ),
                    EventVote(
                        id = 2L,
                        voterName = "Test Name 2",
                        votedDates =
                            listOf(
                                LocalDate.of(2025, 10, 5),
                                LocalDate.of(2025, 10, 6),
                            ),
                        event = event,
                    ),
                ),
            )

            given(eventService.getEventById(1L)).willReturn(event)

            val responseBody =
                mockMvc
                    .get("/api/v1/event/1/results")
                    .andExpect { status { isOk() } }
                    .andReturn()
                    .response
                    .contentAsString

            val response = objectMapper.readValue(responseBody, EventResultResponse::class.java)

            assertEquals(1, response.id, "Response event id should be 1")
            assertEquals("Test Event", response.name, "Response event name should be 'Test Event'")
            assertEquals(1, response.suitableDates.size, "Response event should have exactly 1 suitable date")
            assertEquals(
                LocalDate.of(2025, 10, 5),
                response.suitableDates.first().date,
                "Response events suitable date should be '2025-10-05'",
            )
            assertEquals(
                listOf("Test Name 1", "Test Name 2"),
                response.suitableDates.first().people,
                "Response events suitable date voters should be 'Test Name 1', 'Test Name 2'",
            )
        }

        @Test
        fun `get event results returns empty suitable dates list when no votes are recorded`() {
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

            given(eventService.getEventById(1L)).willReturn(event)

            val responseBody =
                mockMvc
                    .get("/api/v1/event/1/results")
                    .andExpect { status { isOk() } }
                    .andReturn()
                    .response
                    .contentAsString

            val response = objectMapper.readValue(responseBody, EventResultResponse::class.java)

            assertEquals(1, response.id, "Response event id should be 1")
            assertEquals("Test Event", response.name, "Response event name should be 'Test Event'")
            assertTrue(response.suitableDates.isEmpty(), "Response event should have no suitable dates")
        }

        @Test
        fun `get event results returns empty suitable dates list when no date has everyone's vote`() {
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

            event.votes.addAll(
                listOf(
                    EventVote(
                        id = 1L,
                        voterName = "Test Name 1",
                        votedDates = listOf(LocalDate.of(2025, 10, 5)),
                        event = event,
                    ),
                    EventVote(
                        id = 2L,
                        voterName = "Test Name 2",
                        votedDates = listOf(LocalDate.of(2025, 10, 6)),
                        event = event,
                    ),
                ),
            )

            given(eventService.getEventById(1L)).willReturn(event)

            val responseBody =
                mockMvc
                    .get("/api/v1/event/1/results")
                    .andExpect { status { isOk() } }
                    .andReturn()
                    .response
                    .contentAsString

            val response = objectMapper.readValue(responseBody, EventResultResponse::class.java)

            assertEquals(1, response.id, "Response event id should be 1")
            assertEquals("Test Event", response.name, "Response event name should be 'Test Event'")
            assertTrue(response.suitableDates.isEmpty(), "Response event should have no suitable dates")
        }

        @Test
        fun `get event results returns 404 when event not found`() {
            given(eventService.getEventById(999L)).willThrow(EventNotFoundException(999L))

            val responseBody =
                mockMvc
                    .get("/api/v1/event/999/results")
                    .andExpect { status { isNotFound() } }
                    .andReturn()
                    .response
                    .contentAsString

            assertEquals(
                "Event with ID 999 not found",
                responseBody,
                "Error body should contain an indication of invalid event id.",
            )
        }
    }
}
