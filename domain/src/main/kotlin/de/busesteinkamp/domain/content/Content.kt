package de.busesteinkamp.domain.content

import java.util.*

/**
 * Abstract base class representing content in the content management domain.
 * This is an Entity with identity defined by its unique ID.
 * Content has a type and size, and provides access to its data.
 */
abstract class Content(
    val id: UUID = UUID.randomUUID(),
    val contentType: ContentType,
    val size: Long,
) {
    init {
        require(size >= 0) { "Content size cannot be negative" }
    }
    
    abstract fun get(): Any
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Content) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}