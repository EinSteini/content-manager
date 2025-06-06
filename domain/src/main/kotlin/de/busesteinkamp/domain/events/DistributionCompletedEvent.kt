package de.busesteinkamp.domain.events

import java.time.Instant
import java.util.*

/**
 * Domain Event published when a content distribution completes successfully on a platform.
 * This event tracks successful distribution completions.
 */
data class DistributionCompletedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val actorId: String,
    override val createdBy: String = "DistributionAggregate",
    val distributionId: UUID,
    val platformName: String,
    val contentId: UUID,
    val executionDurationMs: Long? = null,
    val platformPostId: String? = null
) : DomainEvent {
    override val eventType: String = "DistributionCompleted"

    companion object {
        /**
         * Factory method to create a DistributionCompletedEvent for successful completion
         */
        fun create(
            distributionId: UUID,
            platformName: String,
            contentId: UUID,
            executionDurationMs: Long? = null,
            platformPostId: String? = null,
            actorId: String = "system"
        ): DistributionCompletedEvent {
            return DistributionCompletedEvent(
                distributionId = distributionId,
                platformName = platformName,
                contentId = contentId,
                executionDurationMs = executionDurationMs,
                platformPostId = platformPostId,
                actorId = actorId
            )
        }
    }
} 