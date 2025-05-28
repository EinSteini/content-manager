package de.busesteinkamp.domain.events

import java.util.*

/**
 * Abstract base class for all Aggregate Roots that support domain events.
 * This class provides the infrastructure for collecting and publishing domain events
 * that occur within the aggregate's business operations.
 */
abstract class AggregateRoot(val id: UUID) {
    private val _domainEvents = mutableListOf<DomainEvent>()
    
    /**
     * Gets all unpublished domain events from this aggregate
     */
    fun getUncommittedEvents(): List<DomainEvent> = _domainEvents.toList()
    
    /**
     * Adds a domain event to the aggregate's event collection
     */
    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }
    
    /**
     * Marks all events as committed/published and clears the event collection
     */
    fun markEventsAsCommitted() {
        _domainEvents.clear()
    }
    
    /**
     * Checks if the aggregate has any uncommitted events
     */
    fun hasUncommittedEvents(): Boolean = _domainEvents.isNotEmpty()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
} 