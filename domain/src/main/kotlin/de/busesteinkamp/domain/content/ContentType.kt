package de.busesteinkamp.domain.content

/**
 * Value Object representing the type of content in the content management domain.
 * This enum encapsulates the supported content types with their MIME type representations.
 * Value Objects are compared by their content, not identity.
 */
enum class ContentType(val mimeType: String) {
    TEXT_PLAIN("text/plain"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_MULTIPLE("image/multiple"),
    VIDEO_MP4("video/mp4");
    
    /**
     * Checks if this content type represents an image.
     */
    fun isImage(): Boolean {
        return this in listOf(IMAGE_JPEG, IMAGE_PNG, IMAGE_MULTIPLE)
    }
    
    /**
     * Checks if this content type represents text.
     */
    fun isText(): Boolean {
        return this == TEXT_PLAIN
    }
    
    /**
     * Checks if this content type represents video.
     */
    fun isVideo(): Boolean {
        return this == VIDEO_MP4
    }
}