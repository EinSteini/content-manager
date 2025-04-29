package de.busesteinkamp.domain.platform

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.process.UploadStatus
import java.util.UUID

abstract class Platform (
    var id: UUID? = null,
    var name: String,
) {
    abstract fun upload(content: Content, publishParameters: PublishParameters, callback: ((UploadStatus) -> Unit)? = null)
}