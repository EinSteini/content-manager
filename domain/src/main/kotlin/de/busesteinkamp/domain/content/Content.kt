package de.busesteinkamp.domain.content

import java.util.*

abstract class Content(
    var id: UUID? = UUID.randomUUID(),
    var contentType: ContentType,
    var size: Long,
) {
    abstract fun get() : Any
}