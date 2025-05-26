package de.busesteinkamp.domain.platform

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.process.UploadStatus
import java.util.UUID

/**
 * Abstract base class representing a social media platform in the content management domain.
 * This is an Entity with identity defined by its unique ID and name.
 * Each platform has specific upload behavior for content distribution.
 */
abstract class SocialMediaPlatform (
    var id: UUID? = null,
    var name: String,
) {
    abstract fun upload(content: Content, publishParameters: PublishParameters, callback: ((UploadStatus) -> Unit)? = null)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SocialMediaPlatform) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}