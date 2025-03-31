package de.busesteinkamp.adapters.content

import de.busesteinkamp.domain.content.Content
import de.busesteinkamp.domain.content.ContentType
import java.util.*

class ImageContent(id: UUID? = UUID.randomUUID(), contentType: ContentType, private val fileContent: ByteArray, val altText: String = "") : Content(
    id,
    contentType,
    fileContent.size.toLong()
){
    override fun get(): ByteArray {
        return fileContent
    }
}