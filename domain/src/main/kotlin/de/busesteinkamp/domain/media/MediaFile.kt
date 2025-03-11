package de.busesteinkamp.domain.media

import de.busesteinkamp.domain.process.UploadStatus
import java.util.UUID

abstract class MediaFile(
    var id: UUID? = UUID.randomUUID(),
    var filename: String,
    var filetype: MediaType,
    var fileSize: Long,
) {
    override fun toString(): String {
        return "MediaFile(id=$id, filename='$filename', filetype='$filetype', fileSize=$fileSize)"
    }

    abstract fun loadFile()
}