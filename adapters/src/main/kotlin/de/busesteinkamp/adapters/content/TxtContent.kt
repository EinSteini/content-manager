package de.busesteinkamp.adapters.content

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentType
import java.util.*

class TxtContent(id: UUID? = UUID.randomUUID(), private val content: String) : Content(
    id,
    contentType = ContentType.TEXT_PLAIN,
    content.length.toLong()
) {
    override fun get(): String {
        return content
    }
}