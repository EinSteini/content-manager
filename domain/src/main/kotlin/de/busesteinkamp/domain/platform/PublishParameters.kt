package de.busesteinkamp.domain.platform

import java.time.Instant
import java.util.*

/**
 * Value Object representing parameters for publishing content to social media platforms.
 * This is immutable and represents a complete set of publishing configuration.
 * Value Objects are compared by their content, not identity.
 */
data class PublishParameters(
    val publishDate: Date = Date.from(Instant.now()),
    val title: String = "",
    val description: String = ""
) {
    init {
        require(title.length <= MAX_TITLE_LENGTH) { 
            "Title cannot exceed $MAX_TITLE_LENGTH characters" 
        }
        require(description.length <= MAX_DESCRIPTION_LENGTH) { 
            "Description cannot exceed $MAX_DESCRIPTION_LENGTH characters" 
        }
    }
    
    companion object {
        private const val MAX_TITLE_LENGTH = 280
        private const val MAX_DESCRIPTION_LENGTH = 1000
        
        fun createDefault(): PublishParameters {
            return PublishParameters()
        }
        
        fun createScheduled(publishDate: Date, title: String = "", description: String = ""): PublishParameters {
            return PublishParameters(publishDate, title, description)
        }
    }
}