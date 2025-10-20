package io.github.tyostokarry.eventshuffle.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.tyostokarry.eventshuffle.dto.EventCreateRequest
import io.github.tyostokarry.eventshuffle.dto.EventCreateResponse
import io.github.tyostokarry.eventshuffle.dto.EventDetailsResponse
import io.github.tyostokarry.eventshuffle.dto.EventListResponse
import io.github.tyostokarry.eventshuffle.entity.Event
import io.github.tyostokarry.eventshuffle.exception.EventNotFoundException
import io.github.tyostokarry.eventshuffle.service.EventService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
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

    @Test
    fun `Get all events returns list`() {
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

    @Test
    fun `Get event by id returns full details`() {
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
    }

    @Test
    fun `Post creates event successfully`() {
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
                .response.contentAsString

        val response = objectMapper.readValue(responseBody, EventCreateResponse::class.java)

        assertEquals(99, response.id, "Response event id should be 99")
    }

    @Test
    fun `Get event by id returns 404 if not found`() {
        given(eventService.getEventById(999))
            .willThrow(EventNotFoundException(999))

        val responseBody =
            mockMvc
                .get("/api/v1/event/999")
                .andExpect { status { isNotFound() } }
                .andReturn()
                .response.contentAsString

        assertEquals("Event with ID 999 not found", responseBody, "Response body should be 'Event with ID 999 not found'")
    }

    @Test
    fun `Post returns 400 when name is blank`() {
        val invalidRequest = EventCreateRequest(name = "   ", dates = listOf(LocalDate.of(2025, 10, 5), LocalDate.of(2025, 10, 12)))

        val body = objectMapper.writeValueAsString(invalidRequest)

        val mvcResult =
            mockMvc
                .post("/api/v1/event") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect { status { isBadRequest() } }
                .andReturn()

        val message = mvcResult.resolvedException?.message
        assertNotNull(message)
        assertTrue(
            message.contains("Event name must not be blank"),
            "Error body should contain 'Event name must not be blank'.",
        )
    }

    @Test
    fun `Post returns 400 when name is missing`() {
        val invalidJson =
            """
            {
                "dates": ["2025-11-05", "2025-11-12"]
            }
            """.trimIndent()

        val mvcResult =
            mockMvc
                .post("/api/v1/event") {
                    contentType = MediaType.APPLICATION_JSON
                    content = invalidJson
                }.andExpect { status { isBadRequest() } }
                .andReturn()

        val message = mvcResult.resolvedException?.message
        assertNotNull(message)
        assertTrue(
            message.contains("value failed for JSON property name due to missing"),
            "Error body should contain 'value failed for JSON property name due to missing'.",
        )
    }

    @Test
    fun `Post returns 400 when empty dates list is given`() {
        val invalidRequest = EventCreateRequest(name = "Test Event", dates = emptyList())

        val body = objectMapper.writeValueAsString(invalidRequest)

        val mvcResult =
            mockMvc
                .post("/api/v1/event") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andExpect { status { isBadRequest() } }
                .andReturn()

        val message = mvcResult.resolvedException?.message
        assertNotNull(message)
        assertTrue(
            message.contains("At least one date must be provided"),
            "Error body should contain 'At least one date must be provided'.",
        )
    }

    @Test
    fun `Post returns 400 when dates are missing`() {
        val invalidJson =
            """
            {
                "name": "Test Event"
            }
            """.trimIndent()

        val mvcResult =
            mockMvc
                .post("/api/v1/event") {
                    contentType = MediaType.APPLICATION_JSON
                    content = invalidJson
                }.andExpect { status { isBadRequest() } }
                .andReturn()

        val message = mvcResult.resolvedException?.message
        assertNotNull(message)
        assertTrue(
            message.contains(" value failed for JSON property dates due to missing"),
            "Error body should contain ' value failed for JSON property dates due to missing'.",
        )
    }

    @Test
    fun `Post returns 400 when dates contain invalid format`() {
        val invalidJson =
            """
            {
                "name": "Test Event",
                "dates": ["not-a-date", "2025-11-12"]
            }
            """.trimIndent()

        val mvcResult =
            mockMvc
                .post("/api/v1/event") {
                    contentType = MediaType.APPLICATION_JSON
                    content = invalidJson
                }.andExpect { status { isBadRequest() } }
                .andReturn()

        val message = mvcResult.resolvedException?.message
        assertNotNull(message)
        assertTrue(
            message.contains("not-a-date") &&
                message.contains("Cannot deserialize value of type"),
            "Error body should contain an indication of invalid date format.",
        )
    }
}
