package io.github.tyostokarry.eventshuffle.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * Represents a participant's vote on available event dates.
 *
 * Each vote links to an event and records which dates the participant is available for.
 */
@Entity
@Table(name = "event_vote")
data class EventVote(
    /**
     * Database-generated primary key for the vote.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    /**
     * Name of the participant casting the vote.
     * Displayed alongside their selected dates.
     */
    @Column(name = "voter_name", nullable = false)
    val voterName: String,
    /**
     * List of dates the participant is available.
     * Stored as an element collection in a separate table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "event_vote_dates",
        joinColumns = [JoinColumn(name = "event_vote_id")],
    )
    @Column(nullable = false)
    val votedDates: List<LocalDate> = emptyList(),
    /**
     * The event this vote belongs to.
     * Defines a many-to-one relationship.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,
)
