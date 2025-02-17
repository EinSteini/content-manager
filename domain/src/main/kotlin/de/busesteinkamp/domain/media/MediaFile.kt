package de.busesteinkamp.domain.media

import de.busesteinkamp.domain.process.UploadStatus
import java.util.UUID

abstract class MediaFile(
    var id: UUID? = UUID.randomUUID(),
    var filename: String,
    var filetype: String,
    var fileSize: Long,
    var uploadStatus: UploadStatus? = UploadStatus.INITIAL
) {
    override fun toString(): String {
        return "MediaFile(id=$id, filename='$filename', filetype='$filetype', fileSize=$fileSize, uploadStatus=$uploadStatus)"
    }

    abstract fun loadFile()
}