package io.github.tyostokarry.eventshuffle.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * Represents an event that can have multiple possible dates.
 *
 * Used by the Eventshuffle app to store event information in the database.
 * Each event is created with a name and a list of potential dates that participants
 * can vote on.
 */
@Entity
@Table(name = "event")
data class Event(
    /**
     * Database-generated primary key for the event.
     * Automatically assigned when a new event is persisted.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    /**
     * Human-readable name of the event (e.g., "Bowling night").
     * Cannot be null. Used to identify and display the event.
     */
    @Column(nullable = false)
    val name: String,
    /**
     * List of possible dates for the event.
     * Participants vote on these dates to find one that suits everyone.
     * Cannot be empty or null.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_dates", joinColumns = [JoinColumn(name = "event_id")])
    @Column(nullable = false)
    val dates: List<LocalDate> = emptyList(),
    /**
     * Votes submitted for this event.
     * Each entry represents a participant’s date selections.
     * Eagerly fetched and cascades all operations.
     */
    @OneToMany(
        mappedBy = "event",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    val votes: MutableList<EventVote> = mutableListOf(),
)
