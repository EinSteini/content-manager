package de.busesteinkamp.domain.events

import java.time.Instant
import java.util.*

/**
 * Domain Event published when a new user is created in the system.
 * This event contains information about the user creation occurrence.
 */
data class UserCreatedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val actorId: String,
    override val createdBy: String = "UserAggregate",
    val userId: UUID,
    val userName: String,
    val platformCount: Int
) : DomainEvent {
    override val eventType: String = "UserCreated"
    
    companion object {
        /**
         * Factory method to create a UserCreatedEvent
         */
        fun create(
            userId: UUID,
            userName: String,
            platformCount: Int,
            actorId: String = "system"
        ): UserCreatedEvent {
            return UserCreatedEvent(
                userId = userId,
                userName = userName,
                platformCount = platformCount,
                actorId = actorId
            )
        }
    }
} 