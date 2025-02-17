package de.busesteinkamp.plugins.media

import de.busesteinkamp.domain.media.MediaFile
import de.busesteinkamp.domain.process.UploadStatus
import java.util.*

class TxtFile(id: UUID?, filename: String, filetype: String, fileSize: Long, uploadStatus: UploadStatus?) : MediaFile(
    id,
    filename,
    filetype,
    fileSize,
    uploadStatus
) {
    override fun loadFile() {
        TODO("Not yet implemented")
    }
}