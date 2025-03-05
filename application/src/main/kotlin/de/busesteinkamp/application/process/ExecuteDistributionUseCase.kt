package de.busesteinkamp.application.process

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
        val mediaFile = distribution.mediaFile
        val publishParameters = distribution.publishParameters

        // Start the distribution process
        platforms.forEach { platform ->
            distribution.reportStatus(platform, UploadStatus.PENDING)
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    uploadToPlatform(mediaFile, platform, publishParameters)
                    distribution.reportStatus(platform, UploadStatus.FINISHED)
                }
            } catch (e: Exception) {
                distribution.reportStatus(platform, UploadStatus.FAILED)
            } finally {
                distributionRepository.update(distribution)
            }
        }
    }

    private suspend fun uploadToPlatform(mediaFile: MediaFile, platform: Platform, publishParameters: PublishParameters) {
        println("Uploading ${mediaFile.filename} to ${platform.name} ...")
        var remainingRetries = 10
        while (!platform.isDoneInitializing()){
            println("Waiting for authorization on ${platform.name} ...")
            remainingRetries--
            println("Retries left: $remainingRetries")
            if (remainingRetries == 0) {
                println("Authorization failed on ${platform.name}")
                return
            }
            kotlinx.coroutines.delay(10000)
        }
        platform.upload(mediaFile, publishParameters)
        println("Uploaded ${mediaFile.filename} to ${platform.name}")
    }
}