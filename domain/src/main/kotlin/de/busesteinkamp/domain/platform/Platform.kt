package de.busesteinkamp.domain.platform

import de.busesteinkamp.domain.media.MediaFile
import java.util.UUID

abstract class Platform (
    var id: UUID? = null,
    var name: String,
) {
    abstract suspend fun upload(mediaFile: MediaFile, publishParameters: PublishParameters)
}