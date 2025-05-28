package de.busesteinkamp.domain.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.events.AggregateRoot
import de.busesteinkamp.domain.events.DistributionCompletedEvent
import de.busesteinkamp.domain.events.DistributionFailedEvent
import de.busesteinkamp.domain.events.DistributionScheduledEvent
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.platform.SocialMediaPlatform
import java.time.Instant
import java.util.*

/**
 * Aggregate Root representing a content distribution process across multiple social media platforms.
 * This entity maintains consistency boundaries for the entire distribution operation.
 * All modifications to the distribution state should go through this aggregate root.
 */
class Distribution private constructor(
    id: UUID,
    val content: Content,
    val publishParameters: PublishParameters,
    private val _platforms: List<SocialMediaPlatform>,
    val createdAt: Date = Date.from(Instant.now())
) : AggregateRoot(id) {
    // Encapsulated collection - external access only through methods
    val platforms: List<SocialMediaPlatform> get() = _platforms.toList()

    private val _uploadStatuses = mutableMapOf<SocialMediaPlatform, UploadStatus>()
    val uploadStatuses: Map<SocialMediaPlatform, UploadStatus> get() = _uploadStatuses.toMap()

    init {
        require(_platforms.isNotEmpty()) { "Distribution must target at least one platform" }
        _platforms.forEach { platform -> _uploadStatuses[platform] = UploadStatus.INITIAL }
    }

    /**
     * Reports the upload status for a specific platform.
     * This method maintains the consistency of the distribution state.
     */
    fun reportStatus(platform: SocialMediaPlatform, status: UploadStatus) {
        require(platform in _platforms) { "Platform ${platform.name} is not part of this distribution" }
        println("Statusreport for platform ${platform.name}: $status")
        
        val previousStatus = _uploadStatuses[platform]
        _uploadStatuses[platform] = status
        
        // Publish domain events based on status changes
        when (status) {
            UploadStatus.FINISHED -> {
                addDomainEvent(
                    DistributionCompletedEvent.create(
                        distributionId = id,
                        platformName = platform.name,
                        contentId = content.id
                    )
                )
            }
            UploadStatus.FAILED -> {
                addDomainEvent(
                    DistributionFailedEvent.create(
                        distributionId = id,
                        platformName = platform.name,
                        reason = "Upload failed",
                        contentId = content.id
                    )
                )
            }
            else -> {
                // No event for INITIAL or PENDING status
            }
        }
    }

    /**
     * Checks if the distribution is complete (all platforms finished or failed).
     */
    fun isComplete(): Boolean {
        return _uploadStatuses.values.all { it == UploadStatus.FINISHED || it == UploadStatus.FAILED }
    }

    /**
     * Checks if the distribution was successful (all platforms finished successfully).
     */
    fun isSuccessful(): Boolean {
        return _uploadStatuses.values.all { it == UploadStatus.FINISHED }
    }

    /**
     * Gets the overall status of the distribution.
     */
    fun getOverallStatus(): DistributionStatus {
        return when {
            _uploadStatuses.values.all { it == UploadStatus.INITIAL } -> DistributionStatus.NOT_STARTED
            _uploadStatuses.values.any { it == UploadStatus.PENDING } -> DistributionStatus.IN_PROGRESS
            isSuccessful() -> DistributionStatus.COMPLETED_SUCCESSFULLY
            _uploadStatuses.values.any { it == UploadStatus.FAILED } -> DistributionStatus.COMPLETED_WITH_FAILURES
            else -> DistributionStatus.COMPLETED_SUCCESSFULLY
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Distribution) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        /**
         * Factory method to create a new distribution.
         * Ensures proper initialization and validation.
         */
        fun create(
            content: Content,
            publishParameters: PublishParameters,
            platforms: List<SocialMediaPlatform>
        ): Distribution {
            val distribution = Distribution(
                id = UUID.randomUUID(),
                content = content,
                publishParameters = publishParameters,
                _platforms = platforms
            )
            
            // Publish domain event for distribution scheduling
            val scheduledTime = publishParameters.publishDate.toInstant()
            distribution.addDomainEvent(
                DistributionScheduledEvent.create(
                    distributionId = distribution.id,
                    scheduledTime = scheduledTime,
                    platformNames = platforms.map { it.name },
                    contentId = content.id,
                    title = publishParameters.title
                )
            )
            
            return distribution
        }
    }
}