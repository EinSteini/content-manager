package de.busesteinkamp.application.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.media.*
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import de.busesteinkamp.domain.process.Distribution
import de.busesteinkamp.domain.process.DistributionRepository
import de.busesteinkamp.domain.process.UploadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                CoroutineScope(Dispatchers.IO).launch {
                    uploadToPlatform(content, platform, publishParameters)
                    distribution.reportStatus(platform, UploadStatus.FINISHED)
                }
            } catch (e: Exception) {
                distribution.reportStatus(platform, UploadStatus.FAILED)
            } finally {
                distributionRepository.update(distribution)
            }
        }
    }

    private suspend fun uploadToPlatform(content: Content, platform: Platform, publishParameters: PublishParameters) {
        platform.upload(content, publishParameters)
        println("Uploaded ${content.get()} to ${platform.name}")
    }
}