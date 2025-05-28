package de.busesteinkamp.domain.events

import java.time.Instant
import java.util.*

/**
 * Domain Event published when a content distribution is scheduled for execution.
 * This event tracks when distributions are planned for future execution.
 */
data class DistributionScheduledEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val actorId: String,
    override val createdBy: String = "DistributionAggregate",
    val distributionId: UUID,
    val scheduledTime: Instant,
    val platformNames: List<String>,
    val contentId: UUID,
    val title: String
) : DomainEvent {
    override val eventType: String = "DistributionScheduled"
    
    companion object {
        /**
         * Factory method to create a DistributionScheduledEvent
         */
        fun create(
            distributionId: UUID,
            scheduledTime: Instant,
            platformNames: List<String>,
            contentId: UUID,
            title: String,
            actorId: String = "system"
        ): DistributionScheduledEvent {
            return DistributionScheduledEvent(
                distributionId = distributionId,
                scheduledTime = scheduledTime,
                platformNames = platformNames,
                contentId = contentId,
                title = title,
                actorId = actorId
            )
        }
    }
} 