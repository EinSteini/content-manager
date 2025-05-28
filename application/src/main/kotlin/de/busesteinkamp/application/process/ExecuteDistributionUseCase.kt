package de.busesteinkamp.application.process

import de.busesteinkamp.domain.events.DomainEventPublisher
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.process.UploadStatus

class ExecuteDistributionUseCase(
    private val distributionRepository: DistributionRepository,
    private val eventPublisher: DomainEventPublisher
) {
    fun execute(distribution: Distribution) {
        // Publish any uncommitted events from distribution creation
        publishUncommittedEvents(distribution)
        
        // Save the distribution to the database
        val savedDistribution = distributionRepository.save(distribution)

        // Get the necessary data from the distribution
        val platforms = distribution.platforms
        val content = distribution.content
        val publishParameters = distribution.publishParameters

        // Start the distribution process
        platforms.forEach { platform ->
            distribution.reportStatus(platform, UploadStatus.PENDING)
            try {
                platform.upload(content, publishParameters) { status ->
                    distribution.reportStatus(platform, status)
                    // Publish events after each status change
                    publishUncommittedEvents(distribution)
                }
            } catch (e: Exception) {
                print(e)
                distribution.reportStatus(platform, UploadStatus.FAILED)
                // Publish failure events
                publishUncommittedEvents(distribution)
            } finally {
                distributionRepository.update(distribution)
            }
        }
    }
    
    private fun publishUncommittedEvents(distribution: Distribution) {
        if (distribution.hasUncommittedEvents()) {
            distribution.getUncommittedEvents().forEach { event ->
                eventPublisher.publish(event)
            }
            distribution.markEventsAsCommitted()
        }
    }
}