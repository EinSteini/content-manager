package de.busesteinkamp.domain.process

/**
 * Value Object representing the overall status of a content distribution process.
 * This enum encapsulates the possible states of a distribution across multiple platforms.
 */
enum class DistributionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED_SUCCESSFULLY,
    COMPLETED_WITH_FAILURES
} 