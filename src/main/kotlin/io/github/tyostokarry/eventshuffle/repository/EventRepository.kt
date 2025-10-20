package io.github.tyostokarry.eventshuffle.repository

import io.github.tyostokarry.eventshuffle.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for accessing and managing Event entities.
 */
@Repository
interface EventRepository : JpaRepository<Event, Long>
