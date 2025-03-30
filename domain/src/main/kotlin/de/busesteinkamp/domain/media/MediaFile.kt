package de.busesteinkamp.domain.media

import de.busesteinkamp.domain.content.ContentType
import java.util.UUID

abstract class MediaFile(
    var id: UUID? = UUID.randomUUID(),
    var filename: String,
    var filetype: ContentType,
    var fileSize: Long,
) {
    override fun toString(): String {
        return "MediaFile(id=$id, filename='$filename', filetype='$filetype', fileSize=$fileSize)"
    }

    abstract fun loadFile()
}