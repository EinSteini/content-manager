package de.busesteinkamp.domain.events

import java.time.Instant
import java.util.*

/**
 * Domain Event published when a content distribution fails on a platform.
 * This event tracks distribution failures with detailed error information.
 */
data class DistributionFailedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val actorId: String,
    override val createdBy: String = "DistributionAggregate",
    val distributionId: UUID,
    val platformName: String,
    val reason: String,
    val contentId: UUID,
    val errorCode: String? = null,
    val retryable: Boolean = false,
    val attemptNumber: Int = 1
) : DomainEvent {
    override val eventType: String = "DistributionFailed"
    
    companion object {
        /**
         * Factory method to create a DistributionFailedEvent
         */
        fun create(
            distributionId: UUID,
            platformName: String,
            reason: String,
            contentId: UUID,
            errorCode: String? = null,
            retryable: Boolean = false,
            attemptNumber: Int = 1,
            actorId: String = "system"
        ): DistributionFailedEvent {
            return DistributionFailedEvent(
                distributionId = distributionId,
                platformName = platformName,
                reason = reason,
                contentId = contentId,
                errorCode = errorCode,
                retryable = retryable,
                attemptNumber = attemptNumber,
                actorId = actorId
            )
        }
    }
} 