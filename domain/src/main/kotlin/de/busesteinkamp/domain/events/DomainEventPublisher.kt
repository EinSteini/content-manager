package de.busesteinkamp.domain.events

/**
 * Interface for publishing domain events.
 * This allows for decoupling between event producers and consumers.
 * Implementations can handle event distribution, persistence, and delivery.
 */
interface DomainEventPublisher {
    /**
     * Publishes a domain event to all interested subscribers
     */
    fun publish(event: DomainEvent)
    
    /**
     * Publishes multiple domain events in a batch
     */
    fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
} 