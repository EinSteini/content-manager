package de.busesteinkamp.domain.platform

import de.busesteinkamp.domain.content.Content
import java.util.UUID

abstract class Platform (
    var id: UUID? = null,
    var name: String,
) {
    abstract suspend fun upload(content: Content, publishParameters: PublishParameters)
}