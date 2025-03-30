package de.busesteinkamp.domain.process

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.platform.Platform
import de.busesteinkamp.domain.platform.PublishParameters
import java.util.*

class Distribution (
    var id: UUID? = null,
    var content: Content,
    var publishParameters: PublishParameters,
    var platforms: List<Platform>,
){
    val uploadStatuses = mutableMapOf<Platform, UploadStatus>()
    init {
        platforms.forEach { platform -> uploadStatuses[platform] = UploadStatus.INITIAL }
    }

    fun reportStatus(platform: Platform, status: UploadStatus){
        uploadStatuses[platform] = status
    }
}