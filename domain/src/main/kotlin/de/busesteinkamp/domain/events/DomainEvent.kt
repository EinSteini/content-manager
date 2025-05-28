package de.busesteinkamp.domain.events

import java.time.Instant
import java.util.*

/**
 * Base interface for all Domain Events in the system.
 * Domain Events represent important business occurrences that have happened in the domain.
 * 
 * Properties of a Domain Event:
 * - What happened? (eventType)
 * - When did it happen? (occurredAt)
 * - Who did it? (actorId)
 * - Who created the event? (createdBy)
 */
interface DomainEvent {
    /**
     * Unique identifier for this event instance
     */
    val eventId: UUID
    
    /**
     * Type of event that occurred (what happened)
     */
    val eventType: String
    
    /**
     * When the event occurred in the domain
     */
    val occurredAt: Instant
    
    /**
     * Who performed the action that caused this event (can be user ID, system, etc.)
     */
    val actorId: String
    
    /**
     * Who/what created this event (usually the aggregate or service that published it)
     */
    val createdBy: String
    
    /**
     * Version of the event schema for evolution purposes
     */
    val version: Int get() = 1
} 