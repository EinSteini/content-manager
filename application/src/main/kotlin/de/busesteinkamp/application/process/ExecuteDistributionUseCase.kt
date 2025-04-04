package de.busesteinkamp.application.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.process.UploadStatus

class ExecuteDistributionUseCase(
    private val distributionRepository: DistributionRepository
) {
    fun execute(distribution: Distribution) {
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
                }
            } catch (e: Exception) {
                distribution.reportStatus(platform, UploadStatus.FAILED)
            } finally {
                distributionRepository.update(distribution)
            }
        }
    }
}